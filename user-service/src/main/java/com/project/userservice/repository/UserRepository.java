package com.project.userservice.repository;

import com.project.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByKeycloakId(String keycloakId);
    
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.active = true")
    Page<User> findByUserTypeAndActiveTrue(@Param("userType") User.UserType userType, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.active = true")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.active = true")
    long countByUserTypeAndActiveTrue(@Param("userType") User.UserType userType);
    
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}