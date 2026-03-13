package com.ecomm.user_service.dto.request;

import com.ecomm.user_service.util.AppConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerRegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = AppConstants.PHONE_REGEX,
            message = "Invalid Indian phone number"
    )
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = AppConstants.PASSWORD_REGEX,
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "GSTIN is required")
    @Pattern(
            regexp = AppConstants.GSTIN_REGEX,
            message = "Invalid GSTIN format"
    )
    private String gstin;
}