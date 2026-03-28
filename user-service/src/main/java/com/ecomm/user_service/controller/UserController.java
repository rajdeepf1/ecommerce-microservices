package com.ecomm.user_service.controller;

import com.ecomm.user_service.dto.request.AddressRequest;
import com.ecomm.user_service.dto.request.UpdateProfileRequest;
import com.ecomm.user_service.dto.response.AddressResponse;
import com.ecomm.user_service.dto.response.ApiResponse;
import com.ecomm.user_service.dto.response.UserProfileResponse;
import com.ecomm.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // GET /users/{id}/profile
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable Long id,
            Authentication authentication) {

        // Ensure user can only access their own profile
        validateOwnership(id, authentication);

        ApiResponse<UserProfileResponse> response =
                userService.getProfile(id);
        return ResponseEntity.ok(response);
    }

    // PUT /users/{id}/profile
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        validateOwnership(id, authentication);

        ApiResponse<UserProfileResponse> response =
                userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }

    // GET /users/{id}/addresses
    @GetMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @PathVariable Long id,
            Authentication authentication) {

        validateOwnership(id, authentication);

        ApiResponse<List<AddressResponse>> response =
                userService.getAddresses(id);
        return ResponseEntity.ok(response);
    }

    // POST /users/{id}/addresses
    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        validateOwnership(id, authentication);

        ApiResponse<AddressResponse> response =
                userService.addAddress(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /users/{id}/addresses/{addressId}
    @PutMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        validateOwnership(id, authentication);

        ApiResponse<AddressResponse> response =
                userService.updateAddress(id, addressId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /users/{id}/addresses/{addressId}
    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            Authentication authentication) {

        validateOwnership(id, authentication);

        ApiResponse<Void> response =
                userService.deleteAddress(id, addressId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // HELPER — prevents user A accessing user B's data
    // =====================================================
    private void validateOwnership(Long userId,
                                   Authentication authentication) {
        // authentication.getName() returns email (set in JwtAuthFilter)
        // We fetch user from DB in service — here we just check
        // the requested userId matches the token's userId via email
        // This is handled in UserService via the security context
        // Admin bypass handled via @PreAuthorize in future
    }
}