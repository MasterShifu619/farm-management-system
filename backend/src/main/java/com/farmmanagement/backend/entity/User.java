package com.farmmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// hibernateLazyInitializer/handler ignored so Jackson can serialize a lazy-loaded
// User (e.g. Farm.owner) without choking on the Hibernate proxy's internal fields
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // BCrypt hash, never returned in API responses
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Only populated for STATE_OFFICIAL; producers' state is derived from their farms
    private String stateCode;

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<Farm> farms = new ArrayList<>();
}
