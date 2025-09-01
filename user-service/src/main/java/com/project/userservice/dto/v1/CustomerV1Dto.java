package com.project.userservice.dto.v1;

import com.project.userservice.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Version 1 Customer DTO
 * 
 * This represents the stable v1 API contract for Customer endpoints.
 * Changes to this class are considered breaking changes and require a new API version.
 * 
 * Compatibility: API v1.x
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerV1Dto {
    
    private Long id;
    private String keycloakId;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    private LocalDate dateOfBirth;
    private Customer.Gender gender;
    
    @Size(max = 500, message = "Preferences must not exceed 500 characters")
    private String preferences;
    
    private Boolean emailNotifications = true;
    private Boolean smsNotifications = false;
    private Boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}