package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.FarmDistanceResponse;
import com.farmmanagement.backend.entity.Movement;
import com.farmmanagement.backend.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<Movement> listMovements() {
        return movementService.listMovements();
    }

    @GetMapping("/traversal")
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<FarmDistanceResponse> findFarmsWithinMovements(
            @RequestParam Long farmId, @RequestParam int hops,
            @RequestParam(required = false) String species) {
        return movementService.findFarmsWithinMovements(farmId, hops, species);
    }
}
