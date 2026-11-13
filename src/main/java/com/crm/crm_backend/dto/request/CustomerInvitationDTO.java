package com.crm.crm_backend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class CustomerInvitationDTO {
    
    @NotBlank
    private String companyName;
    
    @NotBlank
    private String contactName;
    
    @Email
    @NotBlank
    private String email;
    
    private String phone;
    
    @NotBlank
    private String customerType; // GOVERNMENT, PRIVATE, ORGANIZATION, etc.
}
