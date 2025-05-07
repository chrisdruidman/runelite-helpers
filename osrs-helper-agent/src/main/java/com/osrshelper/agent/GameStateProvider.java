package com.osrshelper.agent;

import net.runelite.api.coords.WorldPoint;

public interface GameStateProvider {
    /**
     * @return Array of nearby/interactable object IDs.
     */
    int[] getNearbyObstacleIds();

    /**
     * @return The player's current world position.
     */
    WorldPoint getPlayerWorldPoint();

    /**
     * @param objectId The object ID to search for.
     * @return The WorldPoint of the first matching object, or null if not found.
     */
    WorldPoint getObjectWorldPoint(int objectId);
}
