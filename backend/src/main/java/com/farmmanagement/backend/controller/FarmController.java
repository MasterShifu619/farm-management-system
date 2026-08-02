package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.FarmRequest;
import com.farmmanagement.backend.entity.Farm;
import com.farmmanagement.backend.entity.Role;
import com.farmmanagement.backend.entity.User;
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

// PRODUCER: full CRUD, own farms only. STATE_OFFICIAL: read-only, own state only.
// Role alone isn't enough here — every method also checks ownership/state against
// the DB row, not just the JWT's role claim.
@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmRepository farmRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','STATE_OFFICIAL')")
    public List<Farm> listFarms(Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == Role.PRODUCER) {
            return farmRepository.findByOwnerId(user.getId());
        }
        return farmRepository.findByStateCode(user.getStateCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCER','STATE_OFFICIAL')")
    public Farm getFarm(@PathVariable Long id, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        Farm farm = findFarmOrNotFound(id);
        assertVisible(farm, user);
        return farm;
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    public Farm createFarm(@RequestBody FarmRequest request, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        Farm farm = new Farm();
        farm.setName(request.getName());
        farm.setStateCode(request.getStateCode());
        farm.setOwner(user);
        return farmRepository.save(farm);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public Farm updateFarm(@PathVariable Long id, @RequestBody FarmRequest request, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        Farm farm = findFarmOrNotFound(id);
        assertOwnedBy(farm, user);
        farm.setName(request.getName());
        farm.setStateCode(request.getStateCode());
        return farmRepository.save(farm);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    public void deleteFarm(@PathVariable Long id, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        Farm farm = findFarmOrNotFound(id);
        assertOwnedBy(farm, user);
        farmRepository.delete(farm);
    }

    private Farm findFarmOrNotFound(Long id) {
        return farmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private void assertOwnedBy(Farm farm, User user) {
        if (!farm.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your farm");
        }
    }

    private void assertVisible(Farm farm, User user) {
        boolean visible = user.getRole() == Role.PRODUCER
                ? farm.getOwner().getId().equals(user.getId())
                : farm.getStateCode().equals(user.getStateCode());
        if (!visible) {
            throw new AccessDeniedException("Not permitted to view this farm");
        }
    }
}
