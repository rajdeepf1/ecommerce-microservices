package com.ecomm.user_service.controller;

import com.ecomm.user_service.dto.request.CustomerRegisterRequest;
import com.ecomm.user_service.dto.request.LoginRequest;
import com.ecomm.user_service.dto.request.OtpVerifyRequest;
import com.ecomm.user_service.dto.request.SellerRegisterRequest;
import com.ecomm.user_service.dto.request.TokenRefreshRequest;
import com.ecomm.user_service.dto.response.ApiResponse;
import com.ecomm.user_service.dto.response.AuthResponse;
import com.ecomm.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // POST /auth/register/customer
    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<Void>> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequest request) {

        ApiResponse<Void> response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /auth/register/seller
    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<Void>> registerSeller(
            @Valid @RequestBody SellerRegisterRequest request) {

        ApiResponse<Void> response = authService.registerSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        ApiResponse<AuthResponse> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /auth/otp/verify
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {

        ApiResponse<Void> response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    // POST /auth/token/refresh
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        ApiResponse<AuthResponse> response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}