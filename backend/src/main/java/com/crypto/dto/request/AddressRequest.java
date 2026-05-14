package com.crypto.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @NotBlank(message = "Blockchain is required")
    @Size(max = 50, message = "Blockchain must not exceed 50 characters")
    private String blockchain;
    
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private Boolean favorite = false;
    
    private Set<Long> categoryIds;
}