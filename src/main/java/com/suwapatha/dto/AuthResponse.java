package com.suwapatha.dto;

import com.suwapatha.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String type = "Bearer";

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    private UserRole role;

    private long expiresIn;
}
