package com.ecomm.user_service.service;

import com.ecomm.user_service.dto.request.CustomerRegisterRequest;
import com.ecomm.user_service.dto.request.LoginRequest;
import com.ecomm.user_service.dto.request.OtpVerifyRequest;
import com.ecomm.user_service.dto.request.SellerRegisterRequest;
import com.ecomm.user_service.dto.request.TokenRefreshRequest;
import com.ecomm.user_service.dto.response.ApiResponse;
import com.ecomm.user_service.dto.response.AuthResponse;
import com.ecomm.user_service.entity.Role;
import com.ecomm.user_service.entity.User;
import com.ecomm.user_service.enums.RoleName;
import com.ecomm.user_service.exception.AppException;
import com.ecomm.user_service.repository.RoleRepository;
import com.ecomm.user_service.repository.UserRepository;
import com.ecomm.user_service.security.JwtUtil;
import com.ecomm.user_service.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String OTP_PREFIX = "OTP:";
    private static final long OTP_EXPIRY_SECONDS = 300L;

    // =====================================================
    // CUSTOMER REGISTRATION
    // =====================================================
    @Transactional
    public ApiResponse<Void> registerCustomer(
            CustomerRegisterRequest request) {

        // 1. Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw AppException.conflict("Phone number already registered");
        }

        // 2. Fetch CUSTOMER role from DB
        Role customerRole = roleRepository
                .findByName(RoleName.CUSTOMER.name())
                .orElseThrow(() -> AppException.notFound(
                        "Role not configured. Please contact support"));

        // 3. Build and save user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        userRepository.save(user);

        // 4. Generate OTP and store in Redis
        String otp = generateOtp();
        redisTemplate.opsForValue().set(
                OTP_PREFIX + request.getEmail(),
                otp,
                OTP_EXPIRY_SECONDS,
                TimeUnit.SECONDS
        );

        // TODO: Send OTP via Kafka → Notification Service
        // For now log it for testing
        log.info("OTP for {}: {}", request.getEmail(), otp);

        return ApiResponse.success(
                "Registration successful. " +
                        "Please verify your email with the OTP sent.");
    }

    // =====================================================
    // SELLER REGISTRATION
    // =====================================================
    @Transactional
    public ApiResponse<Void> registerSeller(
            SellerRegisterRequest request) {

        // 1. Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw AppException.conflict("Phone number already registered");
        }

        // 2. Fetch SELLER role
        Role sellerRole = roleRepository
                .findByName(RoleName.SELLER.name())
                .orElseThrow(() -> AppException.notFound(
                        "Role not configured. Please contact support"));

        // 3. Build and save user
        // Note: businessName + gstin are handled by MS-03 Seller Service
        // User Service only stores auth credentials
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(new HashSet<>(Set.of(sellerRole)))
                .build();

        userRepository.save(user);

        // 4. Generate OTP
        String otp = generateOtp();
        redisTemplate.opsForValue().set(
                OTP_PREFIX + request.getEmail(),
                otp,
                OTP_EXPIRY_SECONDS,
                TimeUnit.SECONDS
        );

        // TODO: Publish SELLER_REGISTERED Kafka event
        // MS-03 listens and creates seller profile with businessName + gstin
        log.info("OTP for {}: {}", request.getEmail(), otp);

        return ApiResponse.success(
                "Seller registration successful. " +
                        "Please verify your email.");
    }

    // =====================================================
    // LOGIN
    // =====================================================
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {

        // 1. Authenticate credentials
        // Throws BadCredentialsException or DisabledException automatically
        // GlobalExceptionHandler catches those
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        // 3. Check email verified
        if (!user.isEmailVerified()) {
            throw AppException.forbidden(
                    "Email not verified. " +
                            "Please verify your email before logging in.");
        }

        // 4. Generate tokens
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken  = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 5. Save refresh token in DB for rotation validation
        userRepository.updateRefreshToken(user.getId(), refreshToken);

        // 6. Build response
        AuthResponse authResponse = buildAuthResponse(user,
                accessToken, refreshToken);

        log.info("User logged in: {}", user.getEmail());
        return ApiResponse.success("Login successful", authResponse);
    }

    // =====================================================
    // OTP VERIFICATION
    // =====================================================
    @Transactional
    public ApiResponse<Void> verifyOtp(OtpVerifyRequest request) {

        String redisKey = OTP_PREFIX + request.getEmail();

        // 1. Get OTP from Redis
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        // 2. Check OTP exists (null means expired or never sent)
        if (storedOtp == null) {
            throw AppException.badRequest(
                    "OTP expired or not found. Please request a new one.");
        }

        // 3. Check OTP matches
        if (!storedOtp.equals(request.getOtp())) {
            throw AppException.badRequest(
                    "Invalid OTP. Please try again.");
        }

        // 4. Mark email verified in DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        userRepository.markEmailVerified(user.getId());

        // 5. Delete OTP — one time use only
        redisTemplate.delete(redisKey);

        log.info("Email verified: {}", request.getEmail());
        return ApiResponse.success(
                "Email verified successfully. You can now login.");
    }

    // =====================================================
    // TOKEN REFRESH
    // =====================================================
    @Transactional
    public ApiResponse<AuthResponse> refreshToken(
            TokenRefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        // 1. Validate token structure and expiry
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw AppException.unauthorized(
                    "Invalid or expired refresh token. Please login again.");
        }

        // 2. Extract email
        String email = jwtUtil.extractEmail(refreshToken);

        // 3. Load user and verify stored token matches
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw AppException.unauthorized(
                    "Refresh token mismatch. Please login again.");
        }

        // 4. Generate new tokens
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String newAccessToken  = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 5. Rotate refresh token in DB
        userRepository.updateRefreshToken(user.getId(), newRefreshToken);

        // 6. Build response
        AuthResponse authResponse = buildAuthResponse(user,
                newAccessToken, newRefreshToken);

        log.info("Token refreshed for: {}", email);
        return ApiResponse.success("Token refreshed", authResponse);
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================

    // Generates a cryptographically secure 6-digit OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Builds AuthResponse from User entity + tokens
    private AuthResponse buildAuthResponse(User user,
                                           String accessToken,
                                           String refreshToken) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}