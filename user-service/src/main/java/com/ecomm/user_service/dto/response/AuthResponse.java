package com.ecomm.user_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;       // ["ROLE_CUSTOMER"] — for frontend

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";  // standard JWT token type
}