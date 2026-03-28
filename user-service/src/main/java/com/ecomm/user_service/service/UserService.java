package com.ecomm.user_service.service;

import com.ecomm.user_service.dto.request.AddressRequest;
import com.ecomm.user_service.dto.request.UpdateProfileRequest;
import com.ecomm.user_service.dto.response.AddressResponse;
import com.ecomm.user_service.dto.response.ApiResponse;
import com.ecomm.user_service.dto.response.UserProfileResponse;
import com.ecomm.user_service.entity.Address;
import com.ecomm.user_service.entity.Role;
import com.ecomm.user_service.entity.User;
import com.ecomm.user_service.exception.AppException;
import com.ecomm.user_service.repository.AddressRepository;
import com.ecomm.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // =====================================================
    // GET PROFILE
    // =====================================================
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        return ApiResponse.success("Profile fetched",
                mapToProfileResponse(user));
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================
    @Transactional
    public ApiResponse<UserProfileResponse> updateProfile(
            Long userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        // Only update fields that are provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            if (userRepository.existsByPhone(request.getPhone())
                    && !request.getPhone().equals(user.getPhone())) {
                throw AppException.conflict(
                        "Phone number already in use");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getProfilePic() != null) {
            user.setProfilePic(request.getProfilePic());
        }

        User updatedUser = userRepository.save(user);

        log.info("Profile updated for userId: {}", userId);
        return ApiResponse.success("Profile updated successfully",
                mapToProfileResponse(updatedUser));
    }

    // =====================================================
    // GET ALL ADDRESSES
    // =====================================================
    @Transactional(readOnly = true)
    public ApiResponse<List<AddressResponse>> getAddresses(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw AppException.notFound("User not found");
        }

        List<AddressResponse> addresses = addressRepository
                .findByUserId(userId)
                .stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Addresses fetched", addresses);
    }

    // =====================================================
    // ADD ADDRESS
    // =====================================================
    @Transactional
    public ApiResponse<AddressResponse> addAddress(
            Long userId, AddressRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        AppException.notFound("User not found"));

        // If setting as default, unset all existing defaults first
        if (request.isDefaultAddress()) {
            addressRepository.unsetDefaultAddresses(userId);
        }

        // First address is always default automatically
        boolean isFirstAddress =
                addressRepository.countByUserId(userId) == 0;

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .defaultAddress(request.isDefaultAddress() || isFirstAddress)
                .build();

        Address saved = addressRepository.save(address);

        log.info("Address added for userId: {}", userId);
        return ApiResponse.success("Address added successfully",
                mapToAddressResponse(saved));
    }

    // =====================================================
    // UPDATE ADDRESS
    // =====================================================
    @Transactional
    public ApiResponse<AddressResponse> updateAddress(
            Long userId, Long addressId, AddressRequest request) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        AppException.notFound("Address not found"));

        // Verify this address belongs to requesting user
        if (!address.getUser().getId().equals(userId)) {
            throw AppException.forbidden(
                    "You don't have permission to update this address");
        }

        if (request.isDefaultAddress()) {
            addressRepository.unsetDefaultAddresses(userId);
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(request.isDefaultAddress());

        Address updated = addressRepository.save(address);

        log.info("Address {} updated for userId: {}", addressId, userId);
        return ApiResponse.success("Address updated successfully",
                mapToAddressResponse(updated));
    }

    // =====================================================
    // DELETE ADDRESS
    // =====================================================
    @Transactional
    public ApiResponse<Void> deleteAddress(Long userId, Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        AppException.notFound("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw AppException.forbidden(
                    "You don't have permission to delete this address");
        }

        addressRepository.delete(address);

        log.info("Address {} deleted for userId: {}", addressId, userId);
        return ApiResponse.success("Address deleted successfully");
    }

    // =====================================================
    // MAPPERS
    // =====================================================
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePic(user.getProfilePic())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}