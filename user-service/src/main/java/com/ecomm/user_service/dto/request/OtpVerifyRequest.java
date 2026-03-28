package com.ecomm.user_service.dto.request;

import com.ecomm.user_service.util.AppConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = AppConstants.OTP_REGEX,
            message = "OTP must be 6 digits"
    )
    private String otp;
}