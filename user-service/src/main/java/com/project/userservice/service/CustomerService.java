package com.project.userservice.service;

import com.project.common.exception.BusinessException;
import com.project.userservice.dto.CustomerDto;
import com.project.userservice.entity.Customer;
import com.project.userservice.entity.User;
import com.project.userservice.repository.CustomerRepository;
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
public class CustomerService {
    
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto, String keycloakId) {
        log.info("Creating customer for keycloak ID: {}", keycloakId);
        
        if (customerRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            throw new BusinessException("CUSTOMER_EXISTS", "Customer with this email already exists");
        }

        Customer customer = Customer.builder()
                .keycloakId(keycloakId)
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .email(customerDto.getEmail())
                .phoneNumber(customerDto.getPhoneNumber())
                .userType(User.UserType.CUSTOMER)
                .dateOfBirth(customerDto.getDateOfBirth())
                .gender(customerDto.getGender())
                .preferences(customerDto.getPreferences())
                .emailNotifications(customerDto.getEmailNotifications() != null ? customerDto.getEmailNotifications() : true)
                .smsNotifications(customerDto.getSmsNotifications() != null ? customerDto.getSmsNotifications() : false)
                .active(true)
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", customer.getId());
        return convertToDto(customer);
    }

    public CustomerDto getCustomerByKeycloakId(String keycloakId) {
        Customer customer = customerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        return convertToDto(customer);
    }

    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        return convertToDto(customer);
    }

    public Page<CustomerDto> getAllActiveCustomers(Pageable pageable) {
        return customerRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<CustomerDto> searchCustomers(String searchTerm, Pageable pageable) {
        return customerRepository.searchCustomers(searchTerm, pageable)
                .map(this::convertToDto);
    }

    public List<CustomerDto> getCustomersByGender(Customer.Gender gender) {
        return customerRepository.findByGenderAndActiveTrue(gender)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CustomerDto> getCustomersByAgeRange(LocalDate startDate, LocalDate endDate) {
        return customerRepository.findByDateOfBirthBetweenAndActiveTrue(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        log.info("Updating customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));

        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setDateOfBirth(customerDto.getDateOfBirth());
        customer.setGender(customerDto.getGender());
        customer.setPreferences(customerDto.getPreferences());
        customer.setEmailNotifications(customerDto.getEmailNotifications());
        customer.setSmsNotifications(customerDto.getSmsNotifications());

        customer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", customer.getId());
        return convertToDto(customer);
    }

    @Transactional
    public void deactivateCustomer(Long id) {
        log.info("Deactivating customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        
        customer.setActive(false);
        customerRepository.save(customer);
        log.info("Customer deactivated successfully with ID: {}", id);
    }

    @Transactional
    public void updateNotificationPreferences(Long id, Boolean emailNotifications, Boolean smsNotifications) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        
        customer.setEmailNotifications(emailNotifications);
        customer.setSmsNotifications(smsNotifications);
        customerRepository.save(customer);
    }

    public long getActiveCustomerCount() {
        return customerRepository.countActiveCustomers();
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setKeycloakId(customer.getKeycloakId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setGender(customer.getGender());
        dto.setPreferences(customer.getPreferences());
        dto.setEmailNotifications(customer.getEmailNotifications());
        dto.setSmsNotifications(customer.getSmsNotifications());
        dto.setActive(customer.getActive());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}