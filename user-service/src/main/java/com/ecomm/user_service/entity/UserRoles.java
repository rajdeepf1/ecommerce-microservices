package com.ecomm.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;

    @Column(name="is_Active",nullable = false)
    private Boolean isActive =Boolean.TRUE;

    private Instant create_at;
    private Instant updated_at;


}
