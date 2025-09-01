package com.project.userservice.controller;

import com.project.common.dto.ApiResponse;
import com.project.userservice.dto.BusinessOwnerDto;
import com.project.userservice.entity.BusinessOwner;
import com.project.userservice.service.BusinessOwnerService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/business-owners")
@RequiredArgsConstructor
@Slf4j
public class BusinessOwnerController {
    
    private final BusinessOwnerService businessOwnerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> registerBusinessOwner(
            @Valid @RequestBody BusinessOwnerDto businessOwnerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Registering new business owner: {}", businessOwnerDto.getEmail());
        
        String keycloakId = jwt.getSubject();
        BusinessOwnerDto savedBusinessOwner = businessOwnerService.createBusinessOwner(businessOwnerDto, keycloakId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business owner registered successfully", savedBusinessOwner));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> getBusinessOwnerProfile(
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Getting business owner profile for keycloak ID: {}", jwt.getSubject());
        
        BusinessOwnerDto businessOwner = businessOwnerService.getBusinessOwnerByKeycloakId(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(businessOwner));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (@businessOwnerService.getBusinessOwnerById(#id).keycloakId == authentication.name)")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> getBusinessOwnerById(@PathVariable Long id) {
        log.info("Getting business owner by ID: {}", id);
        
        BusinessOwnerDto businessOwner = businessOwnerService.getBusinessOwnerById(id);
        return ResponseEntity.ok(ApiResponse.success(businessOwner));
    }

    @GetMapping("/business/{businessName}")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> getBusinessOwnerByBusinessName(
            @PathVariable String businessName) {
        log.info("Getting business owner by business name: {}", businessName);
        
        BusinessOwnerDto businessOwner = businessOwnerService.getBusinessOwnerByBusinessName(businessName);
        return ResponseEntity.ok(ApiResponse.success(businessOwner));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BusinessOwnerDto>>> getAllBusinessOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "false") boolean verifiedOnly) {
        log.info("Getting all business owners - page: {}, size: {}, verifiedOnly: {}", page, size, verifiedOnly);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BusinessOwnerDto> businessOwners = verifiedOnly 
                ? businessOwnerService.getAllVerifiedBusinesses(pageable)
                : businessOwnerService.getAllActiveBusinessOwners(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BusinessOwnerDto>>> searchBusinessOwners(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching business owners with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessOwnerDto> businessOwners = businessOwnerService.searchBusinessOwners(searchTerm, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @GetMapping("/by-type/{businessType}")
    public ResponseEntity<ApiResponse<Page<BusinessOwnerDto>>> getBusinessOwnersByType(
            @PathVariable BusinessOwner.BusinessType businessType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting business owners by type: {}", businessType);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("businessName").ascending());
        Page<BusinessOwnerDto> businessOwners = businessOwnerService.getBusinessOwnersByType(businessType, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BusinessOwnerDto>>> getBusinessOwnersByStatus(
            @PathVariable BusinessOwner.BusinessStatus status) {
        log.info("Getting business owners by status: {}", status);
        
        List<BusinessOwnerDto> businessOwners = businessOwnerService.getBusinessOwnersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @GetMapping("/by-established-date")
    public ResponseEntity<ApiResponse<List<BusinessOwnerDto>>> getBusinessesByEstablishedDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Getting businesses established between: {} and {}", startDate, endDate);
        
        List<BusinessOwnerDto> businessOwners = businessOwnerService.getBusinessesByEstablishedDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @GetMapping("/by-employee-count")
    public ResponseEntity<ApiResponse<Page<BusinessOwnerDto>>> getBusinessesByEmployeeCount(
            @RequestParam Integer minCount,
            @RequestParam Integer maxCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting businesses by employee count range: {} - {}", minCount, maxCount);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("employeeCount").descending());
        Page<BusinessOwnerDto> businessOwners = businessOwnerService.getBusinessesByEmployeeCount(minCount, maxCount, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(businessOwners));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> updateBusinessOwnerProfile(
            @Valid @RequestBody BusinessOwnerDto businessOwnerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Updating business owner profile for keycloak ID: {}", jwt.getSubject());
        
        BusinessOwnerDto currentBusinessOwner = businessOwnerService.getBusinessOwnerByKeycloakId(jwt.getSubject());
        BusinessOwnerDto updatedBusinessOwner = businessOwnerService.updateBusinessOwner(
                currentBusinessOwner.getId(), businessOwnerDto);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedBusinessOwner));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessOwnerDto>> updateBusinessOwner(
            @PathVariable Long id,
            @Valid @RequestBody BusinessOwnerDto businessOwnerDto) {
        log.info("Admin updating business owner with ID: {}", id);
        
        BusinessOwnerDto updatedBusinessOwner = businessOwnerService.updateBusinessOwner(id, businessOwnerDto);
        return ResponseEntity.ok(ApiResponse.success("Business owner updated successfully", updatedBusinessOwner));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyBusiness(@PathVariable Long id) {
        log.info("Verifying business with ID: {}", id);
        
        businessOwnerService.verifyBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business verified successfully", null));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> suspendBusiness(@PathVariable Long id) {
        log.info("Suspending business with ID: {}", id);
        
        businessOwnerService.suspendBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business suspended successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateBusinessOwner(@PathVariable Long id) {
        log.info("Deactivating business owner with ID: {}", id);
        
        businessOwnerService.deactivateBusinessOwner(id);
        return ResponseEntity.ok(ApiResponse.success("Business owner deactivated successfully", null));
    }

    @GetMapping("/stats/count-by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getBusinessCountByStatus(
            @PathVariable BusinessOwner.BusinessStatus status) {
        log.info("Getting business count by status: {}", status);
        
        long count = businessOwnerService.getBusinessCountByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/verified-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getVerifiedBusinessCount() {
        log.info("Getting verified business count");
        
        long count = businessOwnerService.getVerifiedBusinessCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/total-employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalEmployeeCount() {
        log.info("Getting total employee count for verified businesses");
        
        Long totalEmployees = businessOwnerService.getTotalEmployeeCount();
        return ResponseEntity.ok(ApiResponse.success(totalEmployees != null ? totalEmployees : 0L));
    }
}