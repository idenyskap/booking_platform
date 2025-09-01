package com.project.userservice.repository;

import com.project.userservice.entity.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    
    Optional<Provider> findByKeycloakId(String keycloakId);
    Optional<Provider> findByEmail(String email);
    
    List<Provider> findByActiveTrue();
    Page<Provider> findByActiveTrue(Pageable pageable);
    
    List<Provider> findByVerifiedTrueAndActiveTrue();
    Page<Provider> findByVerifiedTrueAndActiveTrue(Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE p.status = :status AND p.active = true")
    List<Provider> findByStatusAndActiveTrue(@Param("status") Provider.ProviderStatus status);
    
    @Query("SELECT p FROM Provider p WHERE p.rating >= :minRating AND p.active = true AND p.verified = true")
    Page<Provider> findByRatingGreaterThanEqualAndActiveTrue(@Param("minRating") BigDecimal minRating, Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE p.yearsOfExperience >= :minExperience AND p.active = true AND p.verified = true")
    Page<Provider> findByYearsOfExperienceGreaterThanEqualAndActiveTrue(@Param("minExperience") Integer minExperience, Pageable pageable);
    
    @Query("SELECT p FROM Provider p JOIN p.specializations s WHERE " +
           "LOWER(s) LIKE LOWER(CONCAT('%', :specialization, '%')) AND p.active = true AND p.verified = true")
    Page<Provider> findBySpecializationContainingAndActiveTrue(@Param("specialization") String specialization, Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE " +
           "p.hourlyRate BETWEEN :minRate AND :maxRate AND " +
           "p.active = true AND p.verified = true")
    Page<Provider> findByHourlyRateBetweenAndActiveTrue(@Param("minRate") BigDecimal minRate, 
                                                        @Param("maxRate") BigDecimal maxRate, 
                                                        Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE " +
           "LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')) AND " +
           "p.active = true AND p.verified = true")
    Page<Provider> findByLocationContainingAndActiveTrue(@Param("location") String location, Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.professionalTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.bio) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "p.active = true")
    Page<Provider> searchProviders(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Provider p WHERE p.status = :status AND p.active = true")
    long countByStatusAndActiveTrue(@Param("status") Provider.ProviderStatus status);
    
    @Query("SELECT AVG(p.rating) FROM Provider p WHERE p.verified = true AND p.active = true")
    BigDecimal getAverageRatingForVerifiedProviders();
}