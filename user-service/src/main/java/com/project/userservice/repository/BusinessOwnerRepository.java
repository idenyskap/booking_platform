package com.project.userservice.repository;

import com.project.userservice.entity.BusinessOwner;
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
public interface BusinessOwnerRepository extends JpaRepository<BusinessOwner, Long> {
    
    Optional<BusinessOwner> findByKeycloakId(String keycloakId);
    Optional<BusinessOwner> findByEmail(String email);
    Optional<BusinessOwner> findByBusinessName(String businessName);
    Optional<BusinessOwner> findByTaxId(String taxId);
    Optional<BusinessOwner> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    
    boolean existsByBusinessName(String businessName);
    boolean existsByTaxId(String taxId);
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    
    List<BusinessOwner> findByActiveTrue();
    Page<BusinessOwner> findByActiveTrue(Pageable pageable);
    
    List<BusinessOwner> findByBusinessVerifiedTrueAndActiveTrue();
    Page<BusinessOwner> findByBusinessVerifiedTrueAndActiveTrue(Pageable pageable);
    
    @Query("SELECT bo FROM BusinessOwner bo WHERE bo.businessStatus = :status AND bo.active = true")
    List<BusinessOwner> findByBusinessStatusAndActiveTrue(@Param("status") BusinessOwner.BusinessStatus status);
    
    @Query("SELECT bo FROM BusinessOwner bo WHERE bo.businessType = :type AND bo.active = true")
    Page<BusinessOwner> findByBusinessTypeAndActiveTrue(@Param("type") BusinessOwner.BusinessType type, Pageable pageable);
    
    @Query("SELECT bo FROM BusinessOwner bo WHERE " +
           "bo.businessEstablishedDate BETWEEN :startDate AND :endDate AND bo.active = true")
    List<BusinessOwner> findByBusinessEstablishedDateBetweenAndActiveTrue(@Param("startDate") LocalDate startDate, 
                                                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT bo FROM BusinessOwner bo WHERE " +
           "bo.employeeCount BETWEEN :minCount AND :maxCount AND bo.active = true")
    Page<BusinessOwner> findByEmployeeCountBetweenAndActiveTrue(@Param("minCount") Integer minCount, 
                                                                @Param("maxCount") Integer maxCount, 
                                                                Pageable pageable);
    
    @Query("SELECT bo FROM BusinessOwner bo WHERE " +
           "(LOWER(bo.businessName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bo.businessDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bo.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bo.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "bo.active = true")
    Page<BusinessOwner> searchBusinessOwners(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(bo) FROM BusinessOwner bo WHERE bo.businessStatus = :status AND bo.active = true")
    long countByBusinessStatusAndActiveTrue(@Param("status") BusinessOwner.BusinessStatus status);
    
    @Query("SELECT COUNT(bo) FROM BusinessOwner bo WHERE bo.businessVerified = true AND bo.active = true")
    long countVerifiedBusinesses();
    
    @Query("SELECT SUM(bo.employeeCount) FROM BusinessOwner bo WHERE bo.active = true AND bo.businessVerified = true")
    Long getTotalEmployeeCountForVerifiedBusinesses();
}