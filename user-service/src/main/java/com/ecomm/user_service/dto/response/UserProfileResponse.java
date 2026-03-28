package com.ecomm.user_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String profilePic;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Set<String> roles;
    private Instant createdAt;

    // Notice: NO password, NO refreshToken — never expose these
}