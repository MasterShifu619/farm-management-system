package com.farmmanagement.backend.service;

import com.farmmanagement.backend.dto.PlanRequest;
import com.farmmanagement.backend.entity.*;
import com.farmmanagement.backend.repository.BiosecurityPlanRepository;
import com.farmmanagement.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// PRODUCER: create/edit/submit own farm's plans (DRAFT/REJECTED -> SUBMITTED).
// REVIEWER: view all SUBMITTED plans, approve (-> APPROVED) or reject (-> REJECTED,
// distinct from DRAFT so the producer can see it was actively rejected, not just
// never submitted).
// STATE_OFFICIAL: read-only, plans for farms in their own state.
@Service
@RequiredArgsConstructor
public class BiosecurityPlanService {

    private final BiosecurityPlanRepository planRepository;
    private final FarmRepository farmRepository;

    public List<BiosecurityPlan> listPlans(User user) {
        return switch (user.getRole()) {
            case PRODUCER -> planRepository.findByFarm_Owner_Id(user.getId());
            case REVIEWER -> planRepository.findByStatus(PlanStatus.SUBMITTED);
            case STATE_OFFICIAL -> planRepository.findByFarm_StateCode(user.getStateCode());
        };
    }

    public BiosecurityPlan createPlan(PlanRequest request, User user) {
        Farm farm = farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found"));
        if (!farm.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your farm");
        }

        BiosecurityPlan plan = new BiosecurityPlan();
        plan.setFarm(farm);
        plan.setStatus(PlanStatus.DRAFT);
        applyFields(plan, request);
        return planRepository.save(plan);
    }

    public BiosecurityPlan updatePlan(Long id, PlanRequest request, User user) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        assertOwnsPlanFarm(plan, user);
        if (plan.getStatus() != PlanStatus.DRAFT && plan.getStatus() != PlanStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT or REJECTED plans can be edited");
        }
        applyFields(plan, request);
        return planRepository.save(plan);
    }

    public BiosecurityPlan submitPlan(Long id, User user) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        assertOwnsPlanFarm(plan, user);
        if (plan.getStatus() != PlanStatus.DRAFT && plan.getStatus() != PlanStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT or REJECTED plans can be submitted");
        }
        plan.setStatus(PlanStatus.SUBMITTED);
        return planRepository.save(plan);
    }

    public BiosecurityPlan approvePlan(Long id) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        if (plan.getStatus() != PlanStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SUBMITTED plans can be approved");
        }
        plan.setStatus(PlanStatus.APPROVED);
        return planRepository.save(plan);
    }

    public BiosecurityPlan rejectPlan(Long id) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        if (plan.getStatus() != PlanStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SUBMITTED plans can be rejected");
        }
        plan.setStatus(PlanStatus.REJECTED);
        return planRepository.save(plan);
    }

    private BiosecurityPlan findPlanOrNotFound(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
    }

    private void assertOwnsPlanFarm(BiosecurityPlan plan, User user) {
        if (!plan.getFarm().getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your farm's plan");
        }
    }

    private void applyFields(BiosecurityPlan plan, PlanRequest request) {
        plan.setHasPerimeterFencing(request.isHasPerimeterFencing());
        plan.setHasVisitorLog(request.isHasVisitorLog());
        plan.setHasDisinfectionProtocol(request.isHasDisinfectionProtocol());
        plan.setNotes(request.getNotes());
    }
}
