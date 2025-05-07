package com.osrshelper.agent;

import net.runelite.api.coords.WorldPoint;

public class DummyGameStateProvider implements GameStateProvider {
    @Override
    public int[] getNearbyObstacleIds() {
        // Dummy data for testing
        return new int[] {14843, 14844};
    }

    @Override
    public WorldPoint getPlayerWorldPoint() {
        // Dummy position for testing
        return new WorldPoint(3500, 3500, 0);
    }
}
