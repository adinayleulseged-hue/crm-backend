package com.crm.crm_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileUpdateDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password; // optional: if provided, will be encoded
}
