package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.PlanRequest;
import com.farmmanagement.backend.entity.BiosecurityPlan;
import com.farmmanagement.backend.security.CurrentUserService;
import com.farmmanagement.backend.service.BiosecurityPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Thin HTTP layer: resolve the caller, delegate to BiosecurityPlanService for
// everything else. Status-transition and ownership rules live in the service.
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class BiosecurityPlanController {

    private final BiosecurityPlanService planService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<BiosecurityPlan> listPlans(Authentication authentication) {
        return planService.listPlans(currentUserService.getCurrentUser(authentication));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan createPlan(@RequestBody PlanRequest request, Authentication authentication) {
        return planService.createPlan(request, currentUserService.getCurrentUser(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan updatePlan(@PathVariable Long id, @RequestBody PlanRequest request,
                                       Authentication authentication) {
        return planService.updatePlan(id, request, currentUserService.getCurrentUser(authentication));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('PRODUCER')")
    public BiosecurityPlan submitPlan(@PathVariable Long id, Authentication authentication) {
        return planService.submitPlan(id, currentUserService.getCurrentUser(authentication));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('REVIEWER')")
    public BiosecurityPlan approvePlan(@PathVariable Long id) {
        return planService.approvePlan(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('REVIEWER')")
    public BiosecurityPlan rejectPlan(@PathVariable Long id) {
        return planService.rejectPlan(id);
    }
}
