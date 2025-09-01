package com.project.userservice.service;

import com.project.common.exception.BusinessException;
import com.project.userservice.dto.BusinessOwnerDto;
import com.project.userservice.entity.BusinessOwner;
import com.project.userservice.entity.User;
import com.project.userservice.repository.BusinessOwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessOwnerService {
    
    private final BusinessOwnerRepository businessOwnerRepository;

    @Transactional
    public BusinessOwnerDto createBusinessOwner(BusinessOwnerDto businessOwnerDto, String keycloakId) {
        log.info("Creating business owner for keycloak ID: {}", keycloakId);
        
        if (businessOwnerRepository.findByEmail(businessOwnerDto.getEmail()).isPresent()) {
            throw new BusinessException("BUSINESS_OWNER_EXISTS", "Business owner with this email already exists");
        }

        if (businessOwnerDto.getBusinessName() != null && 
            businessOwnerRepository.existsByBusinessName(businessOwnerDto.getBusinessName())) {
            throw new BusinessException("BUSINESS_NAME_EXISTS", "Business with this name already exists");
        }

        if (businessOwnerDto.getTaxId() != null && 
            businessOwnerRepository.existsByTaxId(businessOwnerDto.getTaxId())) {
            throw new BusinessException("TAX_ID_EXISTS", "Business with this tax ID already exists");
        }

        BusinessOwner businessOwner = BusinessOwner.builder()
                .keycloakId(keycloakId)
                .firstName(businessOwnerDto.getFirstName())
                .lastName(businessOwnerDto.getLastName())
                .email(businessOwnerDto.getEmail())
                .phoneNumber(businessOwnerDto.getPhoneNumber())
                .userType(User.UserType.BUSINESS_OWNER)
                .businessName(businessOwnerDto.getBusinessName())
                .businessDescription(businessOwnerDto.getBusinessDescription())
                .businessType(businessOwnerDto.getBusinessType())
                .taxId(businessOwnerDto.getTaxId())
                .businessRegistrationNumber(businessOwnerDto.getBusinessRegistrationNumber())
                .businessEstablishedDate(businessOwnerDto.getBusinessEstablishedDate())
                .website(businessOwnerDto.getWebsite())
                .employeeCount(businessOwnerDto.getEmployeeCount() != null ? businessOwnerDto.getEmployeeCount() : 0)
                .businessStatus(BusinessOwner.BusinessStatus.PENDING_VERIFICATION)
                .businessVerified(false)
                .active(true)
                .build();

        businessOwner = businessOwnerRepository.save(businessOwner);
        log.info("Business owner created successfully with ID: {}", businessOwner.getId());
        return convertToDto(businessOwner);
    }

    public BusinessOwnerDto getBusinessOwnerByKeycloakId(String keycloakId) {
        BusinessOwner businessOwner = businessOwnerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));
        return convertToDto(businessOwner);
    }

    public BusinessOwnerDto getBusinessOwnerById(Long id) {
        BusinessOwner businessOwner = businessOwnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));
        return convertToDto(businessOwner);
    }

    public BusinessOwnerDto getBusinessOwnerByBusinessName(String businessName) {
        BusinessOwner businessOwner = businessOwnerRepository.findByBusinessName(businessName)
                .orElseThrow(() -> new BusinessException("BUSINESS_NOT_FOUND", "Business not found"));
        return convertToDto(businessOwner);
    }

    public Page<BusinessOwnerDto> getAllActiveBusinessOwners(Pageable pageable) {
        return businessOwnerRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<BusinessOwnerDto> getAllVerifiedBusinesses(Pageable pageable) {
        return businessOwnerRepository.findByBusinessVerifiedTrueAndActiveTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<BusinessOwnerDto> searchBusinessOwners(String searchTerm, Pageable pageable) {
        return businessOwnerRepository.searchBusinessOwners(searchTerm, pageable)
                .map(this::convertToDto);
    }

    public Page<BusinessOwnerDto> getBusinessOwnersByType(BusinessOwner.BusinessType businessType, Pageable pageable) {
        return businessOwnerRepository.findByBusinessTypeAndActiveTrue(businessType, pageable)
                .map(this::convertToDto);
    }

    public List<BusinessOwnerDto> getBusinessOwnersByStatus(BusinessOwner.BusinessStatus status) {
        return businessOwnerRepository.findByBusinessStatusAndActiveTrue(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BusinessOwnerDto> getBusinessesByEstablishedDateRange(LocalDate startDate, LocalDate endDate) {
        return businessOwnerRepository.findByBusinessEstablishedDateBetweenAndActiveTrue(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<BusinessOwnerDto> getBusinessesByEmployeeCount(Integer minCount, Integer maxCount, Pageable pageable) {
        return businessOwnerRepository.findByEmployeeCountBetweenAndActiveTrue(minCount, maxCount, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public BusinessOwnerDto updateBusinessOwner(Long id, BusinessOwnerDto businessOwnerDto) {
        log.info("Updating business owner with ID: {}", id);
        
        BusinessOwner businessOwner = businessOwnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));

        // Check for business name uniqueness if changed
        if (!businessOwner.getBusinessName().equals(businessOwnerDto.getBusinessName()) &&
            businessOwnerRepository.existsByBusinessName(businessOwnerDto.getBusinessName())) {
            throw new BusinessException("BUSINESS_NAME_EXISTS", "Business with this name already exists");
        }

        businessOwner.setFirstName(businessOwnerDto.getFirstName());
        businessOwner.setLastName(businessOwnerDto.getLastName());
        businessOwner.setPhoneNumber(businessOwnerDto.getPhoneNumber());
        businessOwner.setBusinessName(businessOwnerDto.getBusinessName());
        businessOwner.setBusinessDescription(businessOwnerDto.getBusinessDescription());
        businessOwner.setBusinessType(businessOwnerDto.getBusinessType());
        businessOwner.setWebsite(businessOwnerDto.getWebsite());
        businessOwner.setEmployeeCount(businessOwnerDto.getEmployeeCount());

        businessOwner = businessOwnerRepository.save(businessOwner);
        log.info("Business owner updated successfully with ID: {}", businessOwner.getId());
        return convertToDto(businessOwner);
    }

    @Transactional
    public void verifyBusiness(Long id) {
        log.info("Verifying business with ID: {}", id);
        
        BusinessOwner businessOwner = businessOwnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));
        
        businessOwner.setBusinessVerified(true);
        businessOwner.setBusinessStatus(BusinessOwner.BusinessStatus.VERIFIED);
        businessOwnerRepository.save(businessOwner);
        log.info("Business verified successfully with ID: {}", id);
    }

    @Transactional
    public void suspendBusiness(Long id) {
        log.info("Suspending business with ID: {}", id);
        
        BusinessOwner businessOwner = businessOwnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));
        
        businessOwner.setBusinessStatus(BusinessOwner.BusinessStatus.SUSPENDED);
        businessOwnerRepository.save(businessOwner);
        log.info("Business suspended successfully with ID: {}", id);
    }

    @Transactional
    public void deactivateBusinessOwner(Long id) {
        log.info("Deactivating business owner with ID: {}", id);
        
        BusinessOwner businessOwner = businessOwnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BUSINESS_OWNER_NOT_FOUND", "Business owner not found"));
        
        businessOwner.setActive(false);
        businessOwner.setBusinessStatus(BusinessOwner.BusinessStatus.INACTIVE);
        businessOwnerRepository.save(businessOwner);
        log.info("Business owner deactivated successfully with ID: {}", id);
    }

    public long getBusinessCountByStatus(BusinessOwner.BusinessStatus status) {
        return businessOwnerRepository.countByBusinessStatusAndActiveTrue(status);
    }

    public long getVerifiedBusinessCount() {
        return businessOwnerRepository.countVerifiedBusinesses();
    }

    public Long getTotalEmployeeCount() {
        return businessOwnerRepository.getTotalEmployeeCountForVerifiedBusinesses();
    }

    private BusinessOwnerDto convertToDto(BusinessOwner businessOwner) {
        BusinessOwnerDto dto = new BusinessOwnerDto();
        dto.setId(businessOwner.getId());
        dto.setKeycloakId(businessOwner.getKeycloakId());
        dto.setFirstName(businessOwner.getFirstName());
        dto.setLastName(businessOwner.getLastName());
        dto.setEmail(businessOwner.getEmail());
        dto.setPhoneNumber(businessOwner.getPhoneNumber());
        dto.setBusinessName(businessOwner.getBusinessName());
        dto.setBusinessDescription(businessOwner.getBusinessDescription());
        dto.setBusinessType(businessOwner.getBusinessType());
        dto.setTaxId(businessOwner.getTaxId());
        dto.setBusinessRegistrationNumber(businessOwner.getBusinessRegistrationNumber());
        dto.setBusinessEstablishedDate(businessOwner.getBusinessEstablishedDate());
        dto.setBusinessAddresses(businessOwner.getBusinessAddresses());
        dto.setBusinessLicenses(businessOwner.getBusinessLicenses());
        dto.setWebsite(businessOwner.getWebsite());
        dto.setSocialMediaLinks(businessOwner.getSocialMediaLinks());
        dto.setBusinessVerified(businessOwner.getBusinessVerified());
        dto.setBusinessStatus(businessOwner.getBusinessStatus());
        dto.setEmployeeCount(businessOwner.getEmployeeCount());
        dto.setActive(businessOwner.getActive());
        dto.setCreatedAt(businessOwner.getCreatedAt());
        dto.setUpdatedAt(businessOwner.getUpdatedAt());
        return dto;
    }
}