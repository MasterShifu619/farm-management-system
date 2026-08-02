package com.farmmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_farm_id", nullable = false)
    private Farm sourceFarm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_farm_id", nullable = false)
    private Farm destinationFarm;

    @Column(nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    private int animalCount;

    @Column(nullable = false)
    private String species;
}
