package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.entity.Movement;
import com.farmmanagement.backend.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementRepository movementRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<Movement> listMovements() {
        return movementRepository.findAll();
    }

    // TODO (live-coding target, do not implement ahead of time): given a source
    // farm id and a hop count N, BFS over Movement edges (source -> destination)
    // and return every farm reachable within N hops.
    @GetMapping("/traversal")
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public ResponseEntity<String> findFarmsWithinMovements(
            @RequestParam Long farmId, @RequestParam int hops) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("TODO: BFS traversal over Movement edges, not yet implemented");
    }
}
