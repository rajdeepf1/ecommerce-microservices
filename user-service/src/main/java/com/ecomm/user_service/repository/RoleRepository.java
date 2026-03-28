package com.ecomm.user_service.repository;

import com.ecomm.user_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role by name (used when assigning role to user)
    Optional<Role> findByName(String name);

    // Check if role exists (used in DataInitializer)
    boolean existsByName(String name);
}