package com.ecomm.user_service.repository;

import com.ecomm.user_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Get all addresses of a user
    List<Address> findByUserId(Long userId);

    // Get default address of a user
    Optional<Address> findByUserIdAndDefaultAddressTrue(Long userId);

    // Count how many addresses a user has
    int countByUserId(Long userId);

    // Unset all default addresses for a user
    // (called before setting a new default)
    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId")
    void unsetDefaultAddresses(@Param("userId") Long userId);


}
