package com.project.userservice.dto;

import com.project.userservice.entity.BusinessOwner;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerDto {
    
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
    
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;
    
    @Size(max = 1000, message = "Business description must not exceed 1000 characters")
    private String businessDescription;
    
    private BusinessOwner.BusinessType businessType;
    
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
    
    @Size(max = 50, message = "Business registration number must not exceed 50 characters")
    private String businessRegistrationNumber;
    
    private LocalDate businessEstablishedDate;
    private List<BusinessOwner.BusinessAddress> businessAddresses;
    private List<BusinessOwner.BusinessLicense> businessLicenses;
    
    @Size(max = 500, message = "Website must not exceed 500 characters")
    private String website;
    
    private List<BusinessOwner.SocialMediaLink> socialMediaLinks;
    
    private Boolean businessVerified = false;
    private BusinessOwner.BusinessStatus businessStatus = BusinessOwner.BusinessStatus.PENDING_VERIFICATION;
    private Integer employeeCount = 0;
    private Boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}