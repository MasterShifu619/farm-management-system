package com.farmmanagement.backend.repository;

import com.farmmanagement.backend.entity.BiosecurityPlan;
import com.farmmanagement.backend.entity.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiosecurityPlanRepository extends JpaRepository<BiosecurityPlan, Long> {
    List<BiosecurityPlan> findByFarmId(Long farmId);
    List<BiosecurityPlan> findByStatus(PlanStatus status);
}
