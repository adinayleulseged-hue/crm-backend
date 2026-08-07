package com.crm.auth.dto;

import com.crm.auth.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /**
     * Public self-register: EMPLOYEE, SALES_OFFICER, FINANCE_OFFICER only.
     * ADMIN and other elevated roles require an authenticated ADMIN caller.
     * Defaults to EMPLOYEE when omitted.
     */
    private UserRole role = UserRole.EMPLOYEE;

    /** Optional CRM territory assignment for the user. */
    private Long territoryId;
}
