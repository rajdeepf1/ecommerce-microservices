package com.ecomm.user_service.dto.request;

import com.ecomm.user_service.util.AppConstants;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(
            regexp = AppConstants.PHONE_REGEX,
            message = "Invalid Indian phone number"
    )
    private String phone;

    // S3 URL set by backend after upload — client sends filename only
    private String profilePic;
}