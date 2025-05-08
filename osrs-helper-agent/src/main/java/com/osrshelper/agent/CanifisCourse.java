package com.osrshelper.agent;

import java.util.List;
import java.util.Arrays;

public class CanifisCourse implements AgilityCourse {
    // Obstacle step definition: objectId, menu option, menu target
    public static class Obstacle {
        final int id;
        final String option;
        final String target;
        Obstacle(int id, String option, String target) {
            this.id = id;
            this.option = option;
            this.target = target;
        }
    }

    private static final List<Obstacle> OBSTACLES = Arrays.asList(
        new Obstacle(14843, "Climb", "Tree"),         // Start Tree
        new Obstacle(14844, "Jump", "Gap"),           // Gap 1
        new Obstacle(14845, "Jump", "Gap"),           // Gap 2
        new Obstacle(14848, "Jump", "Gap"),           // Gap 3
        new Obstacle(14846, "Jump", "Gap"),           // Gap 4
        new Obstacle(14894, "Vault", "Pole-vault"),   // Pole-vault
        new Obstacle(14847, "Jump", "Gap"),           // Gap 5
        new Obstacle(14897, "Jump-down", "Gap")        // Leap Down
    );

    private int currentStep = 0;
    private final GameStateProvider gameStateProvider;

    public CanifisCourse(GameStateProvider gameStateProvider) {
        this.gameStateProvider = gameStateProvider;
    }

    public String getCurrentObstacleOption() {
        if (currentStep < OBSTACLES.size()) {
            return OBSTACLES.get(currentStep).option;
        }
        return null;
    }

    public String getCurrentObstacleTarget() {
        if (currentStep < OBSTACLES.size()) {
            return OBSTACLES.get(currentStep).target;
        }
        return null;
    }

    @Override
    public int getNextObstacleId() {
        if (currentStep < OBSTACLES.size()) {
            return OBSTACLES.get(currentStep).id;
        }
        return -1;
    }

    @Override
    public void advanceStep() {
        if (currentStep < OBSTACLES.size() - 1) {
            currentStep++;
        }
    }

    @Override
    public void resetCourse() {
        currentStep = 0;
    }

    @Override
    public String getName() {
        return "Canifis Rooftop Course";
    }

    @Override
    public boolean isAtExpectedObstacle(int playerObjectId) {
        return playerObjectId == getNextObstacleId();
    }

    @Override
    public int getActionableObstacleId() {
        int[] playerNearbyObjectIds = gameStateProvider.getNearbyObstacleIds();
        int expectedId = getNextObstacleId();
        for (int id : playerNearbyObjectIds) {
            if (id == expectedId) {
                return expectedId;
            }
        }
        return -1;
    }

    // New: get menu option/target for a given obstacle id (for flexibility)
    public static Obstacle getObstacleById(int id) {
        for (Obstacle o : OBSTACLES) {
            if (o.id == id) return o;
        }
        return null;
    }
}
