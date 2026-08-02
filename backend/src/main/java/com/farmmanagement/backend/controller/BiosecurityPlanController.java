package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.PlanRequest;
import com.farmmanagement.backend.entity.*;
import com.farmmanagement.backend.repository.BiosecurityPlanRepository;
import com.farmmanagement.backend.repository.FarmRepository;
import com.farmmanagement.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// PRODUCER: create/edit/submit own farm's plans (DRAFT -> SUBMITTED).
// REVIEWER: view all SUBMITTED plans, approve or send back to DRAFT.
// STATE_OFFICIAL: read-only, plans for farms in their own state.
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class BiosecurityPlanController {

    private final BiosecurityPlanRepository planRepository;
    private final FarmRepository farmRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<BiosecurityPlan> listPlans(Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return switch (user.getRole()) {
            case PRODUCER -> planRepository.findByFarm_Owner_Id(user.getId());
            case REVIEWER -> planRepository.findByStatus(PlanStatus.SUBMITTED);
            case STATE_OFFICIAL -> planRepository.findByFarm_StateCode(user.getStateCode());
        };
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan createPlan(@RequestBody PlanRequest request, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan updatePlan(@PathVariable Long id, @RequestBody PlanRequest request,
                                       Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        BiosecurityPlan plan = findPlanOrNotFound(id);
        assertOwnsPlanFarm(plan, user);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT plans can be edited");
        }
        applyFields(plan, request);
        return planRepository.save(plan);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan submitPlan(@PathVariable Long id, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        BiosecurityPlan plan = findPlanOrNotFound(id);
        assertOwnsPlanFarm(plan, user);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT plans can be submitted");
        }
        plan.setStatus(PlanStatus.SUBMITTED);
        return planRepository.save(plan);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('REVIEWER')")
    public BiosecurityPlan approvePlan(@PathVariable Long id) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        if (plan.getStatus() != PlanStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SUBMITTED plans can be approved");
        }
        plan.setStatus(PlanStatus.APPROVED);
        return planRepository.save(plan);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('REVIEWER')")
    public BiosecurityPlan rejectPlan(@PathVariable Long id) {
        BiosecurityPlan plan = findPlanOrNotFound(id);
        if (plan.getStatus() != PlanStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only SUBMITTED plans can be rejected");
        }
        plan.setStatus(PlanStatus.DRAFT);
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
