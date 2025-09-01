package com.project.userservice.repository;

import com.project.userservice.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByKeycloakId(String keycloakId);
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByActiveTrue();
    Page<Customer> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.gender = :gender AND c.active = true")
    List<Customer> findByGenderAndActiveTrue(@Param("gender") Customer.Gender gender);
    
    @Query("SELECT c FROM Customer c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate AND c.active = true")
    List<Customer> findByDateOfBirthBetweenAndActiveTrue(@Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM Customer c WHERE c.emailNotifications = true AND c.active = true")
    List<Customer> findCustomersWithEmailNotificationsEnabled();
    
    @Query("SELECT c FROM Customer c WHERE c.smsNotifications = true AND c.active = true")
    List<Customer> findCustomersWithSmsNotificationsEnabled();
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.preferences) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "c.active = true")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true")
    long countActiveCustomers();
}