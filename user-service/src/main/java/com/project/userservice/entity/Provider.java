package com.project.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PROVIDER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Provider extends User {

    @Column(nullable = false)
    @NotBlank(message = "Professional title is required")
    @Size(max = 100, message = "Professional title must not exceed 100 characters")
    private String professionalTitle;

    @Column(length = 1000)
    private String bio;

    @Column(nullable = false)
    private Integer yearsOfExperience = 0;

    @ElementCollection
    @CollectionTable(name = "provider_specializations", joinColumns = @JoinColumn(name = "provider_id"))
    @Column(name = "specialization")
    private List<String> specializations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "provider_certifications", joinColumns = @JoinColumn(name = "provider_id"))
    private List<Certification> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "provider_work_hours", joinColumns = @JoinColumn(name = "provider_id"))
    private List<WorkingHours> workingHours = new ArrayList<>();

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(length = 500)
    private String location;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Enumerated(EnumType.STRING)
    private ProviderStatus status = ProviderStatus.PENDING_VERIFICATION;

    public enum ProviderStatus {
        PENDING_VERIFICATION, VERIFIED, SUSPENDED, INACTIVE
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Certification {
        @Column(nullable = false)
        private String name;

        @Column(nullable = false)
        private String issuingOrganization;

        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialId;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingHours {
        @Enumerated(EnumType.STRING)
        private DayOfWeek dayOfWeek;

        @Column(nullable = false)
        private String startTime;

        @Column(nullable = false)
        private String endTime;

        private Boolean available = true;

        public enum DayOfWeek {
            MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
        }
    }
}