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
    public void run() {
        int nextObstacleId = getNextObstacleId();
        System.out.println("Next obstacle ID: " + nextObstacleId);
        // TODO: Add logic to detect player position and interact with obstacles
    }

    public int getNextObstacleId() {
        if (currentStep < OBSTACLE_IDS.length) {
            return OBSTACLE_IDS[currentStep];
        }
        return -1;
    }

    public void advanceStep() {
        if (currentStep < OBSTACLE_IDS.length - 1) {
            currentStep++;
        }
    }

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
    public boolean isAtExpectedObstacle(int playerObjectId) {
        return playerObjectId == getNextObstacleId();
    }

    /**
     * Placeholder for interacting with the current obstacle.
     * In a real implementation, this would trigger a click or action.
     */
    public void interactWithCurrentObstacle() {
        int obstacleId = getNextObstacleId();
        System.out.println("Interacting with obstacle ID: " + obstacleId);
        // TODO: Implement actual interaction logic
    }

    /**
     * Main automation step: checks if the player is at the correct obstacle, interacts, and advances.
     * Uses the GameStateProvider for modular state access.
     */
    public void step() {
        int[] playerNearbyObjectIds = gameStateProvider.getNearbyObstacleIds();
        int expectedId = getNextObstacleId();
        for (int id : playerNearbyObjectIds) {
            if (id == expectedId) {
                System.out.println("Player is at obstacle " + expectedId + ", interacting and advancing.");
                interactWithCurrentObstacle();
                advanceStep();
                return;
            }
        }
        System.out.println("Player not at expected obstacle. Waiting...");
    }
}
