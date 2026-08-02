package com.farmmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String stateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @JsonIgnore
    @OneToMany(mappedBy = "farm")
    private List<BiosecurityPlan> plans = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "sourceFarm")
    private List<Movement> outgoingMovements = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "destinationFarm")
    private List<Movement> incomingMovements = new ArrayList<>();
}
