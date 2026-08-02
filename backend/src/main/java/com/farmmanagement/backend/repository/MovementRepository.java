package com.farmmanagement.backend.repository;

import com.farmmanagement.backend.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findBySourceFarmId(Long farmId);
    List<Movement> findByDestinationFarmId(Long farmId);
}
