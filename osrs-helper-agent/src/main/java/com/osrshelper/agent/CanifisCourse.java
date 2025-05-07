package com.osrshelper.agent;

public class CanifisCourse implements AgilityCourse {
    // Actual RuneLite object IDs for Canifis obstacles
    private static final int[] OBSTACLE_IDS = {
        14843, // ROOFTOPS_CANIFIS_START_TREE
        14844, // ROOFTOPS_CANIFIS_JUMP
        14845, // ROOFTOPS_CANIFIS_JUMP_2
        14848, // ROOFTOPS_CANIFIS_JUMP_5
        14846, // ROOFTOPS_CANIFIS_JUMP_3
        14894, // ROOFTOPS_CANIFIS_POLEVAULT
        14847, // ROOFTOPS_CANIFIS_JUMP_4
        14897  // ROOFTOPS_CANIFIS_LEAPDOWN
    };

    private int currentStep = 0;
    private final GameStateProvider gameStateProvider;

    public CanifisCourse(GameStateProvider gameStateProvider) {
        this.gameStateProvider = gameStateProvider;
    }

    @Override
    public int getNextObstacleId() {
        if (currentStep < OBSTACLE_IDS.length) {
            return OBSTACLE_IDS[currentStep];
        }
        return -1;
    }

    @Override
    public void advanceStep() {
        if (currentStep < OBSTACLE_IDS.length - 1) {
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

    /**
     * Checks if the player is at the expected obstacle for the current step.
     * @param playerObjectId The object ID the player is currently interacting with or near.
     * @return true if at the correct obstacle, false otherwise.
     */
    @Override
    public boolean isAtExpectedObstacle(int playerObjectId) {
        return playerObjectId == getNextObstacleId();
    }

    /**
     * Main automation step: checks if the player is at the correct obstacle.
     * Returns the obstacle ID if action should be taken, else -1.
     */
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
}
