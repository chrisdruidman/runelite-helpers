package com.osrshelper.agent;

public interface AgilityCourse {
    /**
     * @return The name of the course (e.g., "Canifis Rooftop Course").
     */
    String getName();

    /**
     * Resets the course, preparing for a new run.
     */
    void resetCourse();

    /**
     * Advances the player to the next step in the course.
     */
    void advanceStep();

    /**
     * @return The ID of the next obstacle that the player needs to interact with.
     */
    int getNextObstacleId();

    /**
     * @return The ID of the obstacle that is currently actionable by the player.
     */
    int getActionableObstacleId();

    /**
     * Checks if the player is at the expected obstacle based on the given object ID.
     *
     * @param playerObjectId The object ID of the player.
     * @return True if the player is at the expected obstacle, false otherwise.
     */
    boolean isAtExpectedObstacle(int playerObjectId);
}
