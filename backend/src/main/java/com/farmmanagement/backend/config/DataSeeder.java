package com.farmmanagement.backend.config;

import com.farmmanagement.backend.entity.*;
import com.farmmanagement.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Seeds a small connected dataset on every startup (H2 is in-memory, wiped on restart)
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final BiosecurityPlanRepository planRepository;
    private final MovementRepository movementRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SEED_PASSWORD = "password123";

    @Override
    public void run(String... args) {
        User producer1 = createUser("producer1", Role.PRODUCER, null);
        User producer2 = createUser("producer2", Role.PRODUCER, null);
        User producer3 = createUser("producer3", Role.PRODUCER, null);
        User reviewer1 = createUser("reviewer1", Role.REVIEWER, null);
        User officialNc = createUser("official_nc", Role.STATE_OFFICIAL, "NC");
        User officialSc = createUser("official_sc", Role.STATE_OFFICIAL, "SC");
        userRepository.saveAll(java.util.List.of(
                producer1, producer2, producer3, reviewer1, officialNc, officialSc));

        Farm greenValley = createFarm("Green Valley Farm", "NC", producer1);
        Farm piedmont = createFarm("Piedmont Livestock", "NC", producer2);
        Farm blueRidge = createFarm("Blue Ridge Farm", "NC", producer1);
        Farm lowcountry = createFarm("Lowcountry Farm", "SC", producer3);
        Farm palmetto = createFarm("Palmetto Ranch", "SC", producer3);
        farmRepository.saveAll(java.util.List.of(
                greenValley, piedmont, blueRidge, lowcountry, palmetto));

        planRepository.saveAll(java.util.List.of(
                createPlan(greenValley, PlanStatus.DRAFT, false, false, false,
                        "Initial draft, fencing not yet installed."),
                createPlan(piedmont, PlanStatus.SUBMITTED, true, true, false,
                        "Submitted for review, awaiting disinfection station."),
                createPlan(blueRidge, PlanStatus.APPROVED, true, true, true,
                        "Fully compliant, approved last cycle."),
                createPlan(lowcountry, PlanStatus.SUBMITTED, true, false, true,
                        "Submitted; visitor log process pending."),
                createPlan(palmetto, PlanStatus.DRAFT, false, false, false,
                        "New farm, plan not started.")));

        movementRepository.saveAll(java.util.List.of(
                createMovement(greenValley, piedmont, "2026-06-01", 40, "Cattle"),
                createMovement(piedmont, blueRidge, "2026-06-10", 25, "Cattle"),
                createMovement(greenValley, blueRidge, "2026-06-15", 15, "Cattle"),
                createMovement(blueRidge, lowcountry, "2026-06-20", 60, "Swine"),
                createMovement(lowcountry, palmetto, "2026-06-25", 30, "Swine")));

        System.out.println("=== Seeded users (username / password) ===");
        System.out.println("producer1 / " + SEED_PASSWORD + " (PRODUCER)");
        System.out.println("producer2 / " + SEED_PASSWORD + " (PRODUCER)");
        System.out.println("producer3 / " + SEED_PASSWORD + " (PRODUCER)");
        System.out.println("reviewer1 / " + SEED_PASSWORD + " (REVIEWER)");
        System.out.println("official_nc / " + SEED_PASSWORD + " (STATE_OFFICIAL, NC)");
        System.out.println("official_sc / " + SEED_PASSWORD + " (STATE_OFFICIAL, SC)");
        System.out.println("===========================================");
    }

    private User createUser(String username, Role role, String stateCode) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(SEED_PASSWORD));
        user.setRole(role);
        user.setStateCode(stateCode);
        return user;
    }

    private Farm createFarm(String name, String stateCode, User owner) {
        Farm farm = new Farm();
        farm.setName(name);
        farm.setStateCode(stateCode);
        farm.setOwner(owner);
        return farm;
    }

    private BiosecurityPlan createPlan(Farm farm, PlanStatus status, boolean fencing,
                                        boolean visitorLog, boolean disinfection, String notes) {
        BiosecurityPlan plan = new BiosecurityPlan();
        plan.setFarm(farm);
        plan.setStatus(status);
        plan.setHasPerimeterFencing(fencing);
        plan.setHasVisitorLog(visitorLog);
        plan.setHasDisinfectionProtocol(disinfection);
        plan.setNotes(notes);
        return plan;
    }

    private Movement createMovement(Farm source, Farm destination, String date,
                                     int animalCount, String species) {
        Movement movement = new Movement();
        movement.setSourceFarm(source);
        movement.setDestinationFarm(destination);
        movement.setMovementDate(LocalDate.parse(date));
        movement.setAnimalCount(animalCount);
        movement.setSpecies(species);
        return movement;
    }
}
