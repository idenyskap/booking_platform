package com.project.userservice.service;

import com.project.common.exception.BusinessException;
import com.project.userservice.dto.ProviderDto;
import com.project.userservice.entity.Provider;
import com.project.userservice.entity.User;
import com.project.userservice.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {
    
    private final ProviderRepository providerRepository;

    @Transactional
    public ProviderDto createProvider(ProviderDto providerDto, String keycloakId) {
        log.info("Creating provider for keycloak ID: {}", keycloakId);
        
        if (providerRepository.findByEmail(providerDto.getEmail()).isPresent()) {
            throw new BusinessException("PROVIDER_EXISTS", "Provider with this email already exists");
        }

        Provider provider = Provider.builder()
                .keycloakId(keycloakId)
                .firstName(providerDto.getFirstName())
                .lastName(providerDto.getLastName())
                .email(providerDto.getEmail())
                .phoneNumber(providerDto.getPhoneNumber())
                .userType(User.UserType.PROVIDER)
                .professionalTitle(providerDto.getProfessionalTitle())
                .bio(providerDto.getBio())
                .yearsOfExperience(providerDto.getYearsOfExperience() != null ? providerDto.getYearsOfExperience() : 0)
                .specializations(providerDto.getSpecializations())
                .location(providerDto.getLocation())
                .hourlyRate(providerDto.getHourlyRate())
                .status(Provider.ProviderStatus.PENDING_VERIFICATION)
                .verified(false)
                .active(true)
                .build();

        provider = providerRepository.save(provider);
        log.info("Provider created successfully with ID: {}", provider.getId());
        return convertToDto(provider);
    }

    public ProviderDto getProviderByKeycloakId(String keycloakId) {
        Provider provider = providerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        return convertToDto(provider);
    }

    public ProviderDto getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        return convertToDto(provider);
    }

    public Page<ProviderDto> getAllActiveProviders(Pageable pageable) {
        return providerRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getAllVerifiedProviders(Pageable pageable) {
        return providerRepository.findByVerifiedTrueAndActiveTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> searchProviders(String searchTerm, Pageable pageable) {
        return providerRepository.searchProviders(searchTerm, pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getProvidersBySpecialization(String specialization, Pageable pageable) {
        return providerRepository.findBySpecializationContainingAndActiveTrue(specialization, pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getProvidersByRating(BigDecimal minRating, Pageable pageable) {
        return providerRepository.findByRatingGreaterThanEqualAndActiveTrue(minRating, pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getProvidersByExperience(Integer minExperience, Pageable pageable) {
        return providerRepository.findByYearsOfExperienceGreaterThanEqualAndActiveTrue(minExperience, pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getProvidersByLocation(String location, Pageable pageable) {
        return providerRepository.findByLocationContainingAndActiveTrue(location, pageable)
                .map(this::convertToDto);
    }

    public Page<ProviderDto> getProvidersByPriceRange(BigDecimal minRate, BigDecimal maxRate, Pageable pageable) {
        return providerRepository.findByHourlyRateBetweenAndActiveTrue(minRate, maxRate, pageable)
                .map(this::convertToDto);
    }

    public List<ProviderDto> getProvidersByStatus(Provider.ProviderStatus status) {
        return providerRepository.findByStatusAndActiveTrue(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProviderDto updateProvider(Long id, ProviderDto providerDto) {
        log.info("Updating provider with ID: {}", id);
        
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));

        provider.setFirstName(providerDto.getFirstName());
        provider.setLastName(providerDto.getLastName());
        provider.setPhoneNumber(providerDto.getPhoneNumber());
        provider.setProfessionalTitle(providerDto.getProfessionalTitle());
        provider.setBio(providerDto.getBio());
        provider.setYearsOfExperience(providerDto.getYearsOfExperience());
        provider.setSpecializations(providerDto.getSpecializations());
        provider.setLocation(providerDto.getLocation());
        provider.setHourlyRate(providerDto.getHourlyRate());

        provider = providerRepository.save(provider);
        log.info("Provider updated successfully with ID: {}", provider.getId());
        return convertToDto(provider);
    }

    @Transactional
    public void verifyProvider(Long id) {
        log.info("Verifying provider with ID: {}", id);
        
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        
        provider.setVerified(true);
        provider.setStatus(Provider.ProviderStatus.VERIFIED);
        providerRepository.save(provider);
        log.info("Provider verified successfully with ID: {}", id);
    }

    @Transactional
    public void suspendProvider(Long id) {
        log.info("Suspending provider with ID: {}", id);
        
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        
        provider.setStatus(Provider.ProviderStatus.SUSPENDED);
        providerRepository.save(provider);
        log.info("Provider suspended successfully with ID: {}", id);
    }

    @Transactional
    public void deactivateProvider(Long id) {
        log.info("Deactivating provider with ID: {}", id);
        
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        
        provider.setActive(false);
        provider.setStatus(Provider.ProviderStatus.INACTIVE);
        providerRepository.save(provider);
        log.info("Provider deactivated successfully with ID: {}", id);
    }

    @Transactional
    public void updateProviderRating(Long id, BigDecimal newRating, Integer totalReviews) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Provider not found"));
        
        provider.setRating(newRating);
        provider.setTotalReviews(totalReviews);
        providerRepository.save(provider);
    }

    public long getProviderCountByStatus(Provider.ProviderStatus status) {
        return providerRepository.countByStatusAndActiveTrue(status);
    }

    public BigDecimal getAverageRating() {
        return providerRepository.getAverageRatingForVerifiedProviders();
    }

    private ProviderDto convertToDto(Provider provider) {
        ProviderDto dto = new ProviderDto();
        dto.setId(provider.getId());
        dto.setKeycloakId(provider.getKeycloakId());
        dto.setFirstName(provider.getFirstName());
        dto.setLastName(provider.getLastName());
        dto.setEmail(provider.getEmail());
        dto.setPhoneNumber(provider.getPhoneNumber());
        dto.setProfessionalTitle(provider.getProfessionalTitle());
        dto.setBio(provider.getBio());
        dto.setYearsOfExperience(provider.getYearsOfExperience());
        dto.setSpecializations(provider.getSpecializations());
        dto.setCertifications(provider.getCertifications());
        dto.setWorkingHours(provider.getWorkingHours());
        dto.setRating(provider.getRating());
        dto.setTotalReviews(provider.getTotalReviews());
        dto.setVerified(provider.getVerified());
        dto.setLocation(provider.getLocation());
        dto.setHourlyRate(provider.getHourlyRate());
        dto.setStatus(provider.getStatus());
        dto.setActive(provider.getActive());
        dto.setCreatedAt(provider.getCreatedAt());
        dto.setUpdatedAt(provider.getUpdatedAt());
        return dto;
    }
}