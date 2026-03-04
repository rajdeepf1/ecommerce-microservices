package com.ecomm.user_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String role;

    @Column(name="is_Active",nullable = false)
    private Boolean isActive =Boolean.TRUE;

    @Column(name="created_At")
    private Instant createdAt;

    @Column(name="updated_At")
    private Instant updatedAt;

}

// Nitin