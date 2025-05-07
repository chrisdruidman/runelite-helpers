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
}
