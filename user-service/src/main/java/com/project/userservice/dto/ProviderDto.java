package com.project.userservice.dto;

import com.project.userservice.entity.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDto {
    
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
    
    @NotBlank(message = "Professional title is required")
    @Size(max = 100, message = "Professional title must not exceed 100 characters")
    private String professionalTitle;
    
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
    
    private Integer yearsOfExperience = 0;
    private List<String> specializations;
    private List<Provider.Certification> certifications;
    private List<Provider.WorkingHours> workingHours;
    
    private BigDecimal rating = BigDecimal.ZERO;
    private Integer totalReviews = 0;
    private Boolean verified = false;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    private BigDecimal hourlyRate;
    private Provider.ProviderStatus status = Provider.ProviderStatus.PENDING_VERIFICATION;
    private Boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}