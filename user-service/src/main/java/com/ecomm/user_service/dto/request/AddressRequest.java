package com.ecomm.user_service.dto.request;

import com.ecomm.user_service.util.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = AppConstants.PHONE_REGEX,
            message = "Invalid Indian phone number"
    )
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(
            regexp = AppConstants.PINCODE_REGEX,
            message = "Invalid pincode"
    )
    private String pincode;

    @NotBlank(message = "Country is required")
    private String country;

    private boolean defaultAddress;
}