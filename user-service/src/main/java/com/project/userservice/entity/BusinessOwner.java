package com.project.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("BUSINESS_OWNER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BusinessOwner extends User {

    @Column(nullable = false)
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Column(length = 1000)
    private String businessDescription;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(unique = true)
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Column(unique = true)
    @Size(max = 50, message = "Business registration number must not exceed 50 characters")
    private String businessRegistrationNumber;

    private LocalDate businessEstablishedDate;

    @ElementCollection
    @CollectionTable(name = "business_addresses", joinColumns = @JoinColumn(name = "business_owner_id"))
    private List<BusinessAddress> businessAddresses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "business_licenses", joinColumns = @JoinColumn(name = "business_owner_id"))
    private List<BusinessLicense> businessLicenses = new ArrayList<>();

    @Column(length = 500)
    private String website;

    @ElementCollection
    @CollectionTable(name = "business_social_media", joinColumns = @JoinColumn(name = "business_owner_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "url")
    private List<SocialMediaLink> socialMediaLinks = new ArrayList<>();

    @Column(nullable = false)
    private Boolean businessVerified = false;

    @Enumerated(EnumType.STRING)
    private BusinessStatus businessStatus = BusinessStatus.PENDING_VERIFICATION;

    @Column(nullable = false)
    private Integer employeeCount = 0;

    public enum BusinessType {
        SOLE_PROPRIETORSHIP, PARTNERSHIP, LLC, CORPORATION, NON_PROFIT, OTHER
    }

    public enum BusinessStatus {
        PENDING_VERIFICATION, VERIFIED, SUSPENDED, INACTIVE, CLOSED
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAddress {
        @Column(nullable = false)
        private String street;

        @Column(nullable = false)
        private String city;

        @Column(nullable = false)
        private String state;

        @Column(nullable = false)
        private String zipCode;

        @Column(nullable = false)
        private String country;

        @Enumerated(EnumType.STRING)
        private AddressType type = AddressType.MAIN_OFFICE;

        public enum AddressType {
            MAIN_OFFICE, BRANCH_OFFICE, WAREHOUSE, SERVICE_LOCATION
        }
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessLicense {
        @Column(nullable = false)
        private String licenseName;

        @Column(nullable = false)
        private String licenseNumber;

        @Column(nullable = false)
        private String issuingAuthority;

        private LocalDate issueDate;
        private LocalDate expiryDate;

        @Enumerated(EnumType.STRING)
        private LicenseStatus status = LicenseStatus.ACTIVE;

        public enum LicenseStatus {
            ACTIVE, EXPIRED, SUSPENDED, REVOKED
        }
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialMediaLink {
        @Enumerated(EnumType.STRING)
        private Platform platform;

        @Column(nullable = false)
        private String url;

        public enum Platform {
            FACEBOOK, TWITTER, INSTAGRAM, LINKEDIN, YOUTUBE, TIKTOK, OTHER
        }
    }
}