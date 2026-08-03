package com.farmmanagement.backend.service;

import com.farmmanagement.backend.dto.FarmRequest;
import com.farmmanagement.backend.entity.Farm;
import com.farmmanagement.backend.entity.Role;
import com.farmmanagement.backend.entity.User;
import com.farmmanagement.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// PRODUCER: full CRUD, own farms only. STATE_OFFICIAL: read-only, own state only.
// Role alone isn't enough here — every method also checks ownership/state against
// the DB row, not just the JWT's role claim.
@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;

    public List<Farm> listFarms(User user) {
        if (user.getRole() == Role.PRODUCER) {
            return farmRepository.findByOwnerId(user.getId());
        }
        return farmRepository.findByStateCode(user.getStateCode());
    }

    public Farm getFarm(Long id, User user) {
        Farm farm = findFarmOrNotFound(id);
        assertVisible(farm, user);
        return farm;
    }

    public Farm createFarm(FarmRequest request, User user) {
        Farm farm = new Farm();
        farm.setName(request.getName());
        farm.setStateCode(request.getStateCode());
        farm.setOwner(user);
        return farmRepository.save(farm);
    }

    public Farm updateFarm(Long id, FarmRequest request, User user) {
        Farm farm = findFarmOrNotFound(id);
        assertOwnedBy(farm, user);
        farm.setName(request.getName());
        farm.setStateCode(request.getStateCode());
        return farmRepository.save(farm);
    }

    public void deleteFarm(Long id, User user) {
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
