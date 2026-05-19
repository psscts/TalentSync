package com.pss.SRAS.dto;

import com.pss.SRAS.models.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private UserRole role;
    private Long userId;
}
