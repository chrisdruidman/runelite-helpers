package com.osrs.helper.agent.helpermodules.agility;

/**
 * Represents a world position for agility automation.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This class is part of the hybrid patch-based approach.
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
