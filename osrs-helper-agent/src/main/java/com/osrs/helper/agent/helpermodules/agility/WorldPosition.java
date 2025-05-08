package com.osrs.helper.agent.helpermodules.agility;

/**
 * Simple value class for world position (x, y, plane).
 */
public class WorldPosition {
    public final int x, y, plane;
    public WorldPosition(int x, int y, int plane) {
        this.x = x;
        this.y = y;
        this.plane = plane;
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + plane + ")";
    }
}
