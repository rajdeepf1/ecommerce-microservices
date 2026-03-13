package com.ecomm.user_service.repository;

import com.ecomm.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used during login)
    Optional<User> findByEmail(String email);

    // Find user by phone (used during OTP verification)
    Optional<User> findByPhone(String phone);

    // Check if email already registered (used during registration)
    boolean existsByEmail(String email);

    // Check if phone already registered
    boolean existsByPhone(String phone);

    // Find user by refresh token (used during token refresh)
    Optional<User> findByRefreshToken(String refreshToken);

    // Update refresh token directly without loading full entity
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :token WHERE u.id = :id")
    void updateRefreshToken(@Param("id") Long id, @Param("token") String token);

    // Mark email as verified after OTP confirmation
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    void markEmailVerified(@Param("id") Long id);

    // Enable or disable user account (admin action)
    @Modifying
    @Query("UPDATE User u SET u.enabled = :status WHERE u.id = :id")
    void updateEnabledStatus(@Param("id") Long id, @Param("status") boolean status);
}
