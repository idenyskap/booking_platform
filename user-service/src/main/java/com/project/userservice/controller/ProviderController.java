package com.project.userservice.controller;

import com.project.common.dto.ApiResponse;
import com.project.userservice.dto.ProviderDto;
import com.project.userservice.entity.Provider;
import com.project.userservice.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {
    
    private final ProviderService providerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProviderDto>> registerProvider(
            @Valid @RequestBody ProviderDto providerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Registering new provider: {}", providerDto.getEmail());
        
        String keycloakId = jwt.getSubject();
        ProviderDto savedProvider = providerService.createProvider(providerDto, keycloakId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Provider registered successfully", savedProvider));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProviderDto>> getProviderProfile(
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Getting provider profile for keycloak ID: {}", jwt.getSubject());
        
        ProviderDto provider = providerService.getProviderByKeycloakId(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(provider));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProviderDto>> getProviderById(@PathVariable Long id) {
        log.info("Getting provider by ID: {}", id);
        
        ProviderDto provider = providerService.getProviderById(id);
        return ResponseEntity.ok(ApiResponse.success(provider));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getAllProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "false") boolean verifiedOnly) {
        log.info("Getting all providers - page: {}, size: {}, verifiedOnly: {}", page, size, verifiedOnly);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProviderDto> providers = verifiedOnly 
                ? providerService.getAllVerifiedProviders(pageable)
                : providerService.getAllActiveProviders(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> searchProviders(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching providers with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProviderDto> providers = providerService.searchProviders(searchTerm, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-specialization")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getProvidersBySpecialization(
            @RequestParam String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting providers by specialization: {}", specialization);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        Page<ProviderDto> providers = providerService.getProvidersBySpecialization(specialization, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-rating")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getProvidersByRating(
            @RequestParam BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting providers with minimum rating: {}", minRating);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        Page<ProviderDto> providers = providerService.getProvidersByRating(minRating, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-experience")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getProvidersByExperience(
            @RequestParam Integer minExperience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting providers with minimum experience: {} years", minExperience);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("yearsOfExperience").descending());
        Page<ProviderDto> providers = providerService.getProvidersByExperience(minExperience, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-location")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getProvidersByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting providers by location: {}", location);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        Page<ProviderDto> providers = providerService.getProvidersByLocation(location, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-price-range")
    public ResponseEntity<ApiResponse<Page<ProviderDto>>> getProvidersByPriceRange(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting providers by price range: {} - {}", minRate, maxRate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("hourlyRate").ascending());
        Page<ProviderDto> providers = providerService.getProvidersByPriceRange(minRate, maxRate, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProviderDto>>> getProvidersByStatus(
            @PathVariable Provider.ProviderStatus status) {
        log.info("Getting providers by status: {}", status);
        
        List<ProviderDto> providers = providerService.getProvidersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<ProviderDto>> updateProviderProfile(
            @Valid @RequestBody ProviderDto providerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Updating provider profile for keycloak ID: {}", jwt.getSubject());
        
        ProviderDto currentProvider = providerService.getProviderByKeycloakId(jwt.getSubject());
        ProviderDto updatedProvider = providerService.updateProvider(currentProvider.getId(), providerDto);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProvider));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProviderDto>> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderDto providerDto) {
        log.info("Admin updating provider with ID: {}", id);
        
        ProviderDto updatedProvider = providerService.updateProvider(id, providerDto);
        return ResponseEntity.ok(ApiResponse.success("Provider updated successfully", updatedProvider));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyProvider(@PathVariable Long id) {
        log.info("Verifying provider with ID: {}", id);
        
        providerService.verifyProvider(id);
        return ResponseEntity.ok(ApiResponse.success("Provider verified successfully", null));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> suspendProvider(@PathVariable Long id) {
        log.info("Suspending provider with ID: {}", id);
        
        providerService.suspendProvider(id);
        return ResponseEntity.ok(ApiResponse.success("Provider suspended successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateProvider(@PathVariable Long id) {
        log.info("Deactivating provider with ID: {}", id);
        
        providerService.deactivateProvider(id);
        return ResponseEntity.ok(ApiResponse.success("Provider deactivated successfully", null));
    }

    @PutMapping("/{id}/rating")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSTEM')")
    public ResponseEntity<ApiResponse<Void>> updateProviderRating(
            @PathVariable Long id,
            @RequestParam BigDecimal newRating,
            @RequestParam Integer totalReviews) {
        log.info("Updating rating for provider ID: {} to {}", id, newRating);
        
        providerService.updateProviderRating(id, newRating, totalReviews);
        return ResponseEntity.ok(ApiResponse.success("Provider rating updated successfully", null));
    }

    @GetMapping("/stats/count-by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getProviderCountByStatus(
            @PathVariable Provider.ProviderStatus status) {
        log.info("Getting provider count by status: {}", status);
        
        long count = providerService.getProviderCountByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/average-rating")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageRating() {
        log.info("Getting average rating for verified providers");
        
        BigDecimal averageRating = providerService.getAverageRating();
        return ResponseEntity.ok(ApiResponse.success(averageRating));
    }
}