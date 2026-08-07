package com.crm.auth.dto;

import lombok.Data;

@Data
public class LogoutRequestDTO {

    private String refreshToken;
}
