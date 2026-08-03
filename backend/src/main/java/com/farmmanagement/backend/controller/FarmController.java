package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.FarmRequest;
import com.farmmanagement.backend.entity.Farm;
import com.farmmanagement.backend.security.CurrentUserService;
import com.farmmanagement.backend.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Thin HTTP layer: resolve the caller, delegate to FarmService for everything
// else. Ownership/visibility rules live in the service, not here.
@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','STATE_OFFICIAL')")
    public List<Farm> listFarms(Authentication authentication) {
        return farmService.listFarms(currentUserService.getCurrentUser(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCER','STATE_OFFICIAL')")
    public Farm getFarm(@PathVariable Long id, Authentication authentication) {
        return farmService.getFarm(id, currentUserService.getCurrentUser(authentication));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    public Farm createFarm(@RequestBody FarmRequest request, Authentication authentication) {
        return farmService.createFarm(request, currentUserService.getCurrentUser(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public Farm updateFarm(@PathVariable Long id, @RequestBody FarmRequest request, Authentication authentication) {
        return farmService.updateFarm(id, request, currentUserService.getCurrentUser(authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public void deleteFarm(@PathVariable Long id, Authentication authentication) {
        farmService.deleteFarm(id, currentUserService.getCurrentUser(authentication));
    }
}
