package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.FarmDistanceResponse;
import com.farmmanagement.backend.entity.Farm;
import com.farmmanagement.backend.entity.Movement;
import com.farmmanagement.backend.repository.FarmRepository;
import com.farmmanagement.backend.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementRepository movementRepository;
    private final FarmRepository farmRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<Movement> listMovements() {
        return movementRepository.findAll();
    }

    // Given a source farm and a hop count N, walk Movement edges (source ->
    // destination, directed) level by level and return every farm reachable
    // within N hops, tagged with its shortest hop distance.
    //
    // Chronology matters here: a chain A->B->C is only a valid exposure path
    // if the B->C movement happened on or after the A->B movement. Otherwise
    // you'd be tracing exposure backwards in time. So each hop only follows
    // edges dated on/after the date exposure could have arrived at that farm.
    // When multiple edges reach the same farm in the same level, we keep the
    // earliest qualifying date — it's the most permissive for continuing
    // (more future edges satisfy ">= an earlier date" than a later one).
    //
    // species is optional: some diseases only spread through a specific
    // species, so a null/omitted species traces every movement, while a
    // given species only follows edges carrying that species.
    @GetMapping("/traversal")
    @PreAuthorize("hasAnyRole('PRODUCER','REVIEWER','STATE_OFFICIAL')")
    public List<FarmDistanceResponse> findFarmsWithinMovements(
            @RequestParam Long farmId, @RequestParam int hops,
            @RequestParam(required = false) String species) {
        if (!farmRepository.existsById(farmId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found");
        }
        if (hops < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hops must be >= 0");
        }
        boolean filterBySpecies = species != null && !species.isBlank();

        Map<Long, Integer> hopsMap = new LinkedHashMap<>();
        Map<Long, LocalDate> earliestExposure = new HashMap<>();
        hopsMap.put(farmId, 0);
        earliestExposure.put(farmId, LocalDate.MIN); // source: no chronological constraint yet

        List<Long> currentLevel = List.of(farmId);
        int currentHop = 0;

        while (currentHop < hops && !currentLevel.isEmpty()) {
            Map<Long, LocalDate> nextLevel = new LinkedHashMap<>();
            for (Long current : currentLevel) {
                LocalDate constraintDate = earliestExposure.get(current);
                List<Movement> outgoing = filterBySpecies
                        ? movementRepository.findBySourceFarmIdAndSpecies(current, species)
                        : movementRepository.findBySourceFarmId(current);
                for (Movement movement : outgoing) {
                    if (movement.getMovementDate().isBefore(constraintDate)) {
                        continue; // this shipment happened before exposure could have arrived here
                    }
                    Long neighborId = movement.getDestinationFarm().getId();
                    if (hopsMap.containsKey(neighborId)) {
                        continue; // already reached at an earlier (or equal) hop level
                    }
                    LocalDate existing = nextLevel.get(neighborId);
                    if (existing == null || movement.getMovementDate().isBefore(existing)) {
                        nextLevel.put(neighborId, movement.getMovementDate());
                    }
                }
            }
            currentHop++;
            for (Map.Entry<Long, LocalDate> entry : nextLevel.entrySet()) {
                hopsMap.put(entry.getKey(), currentHop);
                earliestExposure.put(entry.getKey(), entry.getValue());
            }
            currentLevel = new ArrayList<>(nextLevel.keySet());
        }

        hopsMap.remove(farmId); // exclude the source farm itself

        List<FarmDistanceResponse> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : hopsMap.entrySet()) {
            Farm farm = farmRepository.findById(entry.getKey()).orElseThrow();
            result.add(new FarmDistanceResponse(
                    farm.getId(), farm.getName(), farm.getStateCode(),
                    entry.getValue(), earliestExposure.get(entry.getKey())));
        }
        result.sort(Comparator.comparingInt(FarmDistanceResponse::getHops));
        return result;
    }
}
