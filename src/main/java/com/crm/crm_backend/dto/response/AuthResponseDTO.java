package com.crm.crm_backend.dto.response;

import com.crm.crm_backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String tokenType;
    private Long expiresInMs;
    private String username;
    private UserRole role;
}
