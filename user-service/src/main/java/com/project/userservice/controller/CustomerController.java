package com.project.userservice.controller;

import com.project.common.dto.ApiResponse;
import com.project.userservice.dto.CustomerDto;
import com.project.userservice.entity.Customer;
import com.project.userservice.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerDto>> registerCustomer(
            @Valid @RequestBody CustomerDto customerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Registering new customer: {}", customerDto.getEmail());
        
        String keycloakId = jwt.getSubject();
        CustomerDto savedCustomer = customerService.createCustomer(customerDto, keycloakId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", savedCustomer));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerProfile(
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Getting customer profile for keycloak ID: {}", jwt.getSubject());
        
        CustomerDto customer = customerService.getCustomerByKeycloakId(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (@customerService.getCustomerById(#id).keycloakId == authentication.name)")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable Long id) {
        log.info("Getting customer by ID: {}", id);
        
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Getting all customers - page: {}, size: {}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerDto> customers = customerService.getAllActiveCustomers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> searchCustomers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching customers with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDto> customers = customerService.searchCustomers(searchTerm, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/by-gender/{gender}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getCustomersByGender(
            @PathVariable Customer.Gender gender) {
        log.info("Getting customers by gender: {}", gender);
        
        List<CustomerDto> customers = customerService.getCustomersByGender(gender);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/by-age-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getCustomersByAgeRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Getting customers by age range: {} to {}", startDate, endDate);
        
        List<CustomerDto> customers = customerService.getCustomersByAgeRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomerProfile(
            @Valid @RequestBody CustomerDto customerDto,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Updating customer profile for keycloak ID: {}", jwt.getSubject());
        
        // Get current customer to obtain ID
        CustomerDto currentCustomer = customerService.getCustomerByKeycloakId(jwt.getSubject());
        CustomerDto updatedCustomer = customerService.updateCustomer(currentCustomer.getId(), customerDto);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedCustomer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto) {
        log.info("Admin updating customer with ID: {}", id);
        
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updatedCustomer));
    }

    @PutMapping("/notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> updateNotificationPreferences(
            @RequestParam Boolean emailNotifications,
            @RequestParam Boolean smsNotifications,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Updating notification preferences for keycloak ID: {}", jwt.getSubject());
        
        CustomerDto currentCustomer = customerService.getCustomerByKeycloakId(jwt.getSubject());
        customerService.updateNotificationPreferences(
                currentCustomer.getId(), emailNotifications, smsNotifications);
        
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(@PathVariable Long id) {
        log.info("Deactivating customer with ID: {}", id);
        
        customerService.deactivateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deactivated successfully", null));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getActiveCustomerCount() {
        log.info("Getting active customer count");
        
        long count = customerService.getActiveCustomerCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}