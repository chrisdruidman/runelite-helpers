package com.osrs.helper.agent.helpermodules.agility;

import com.osrs.helper.agent.helpermodules.agility.WorldPosition;

/**
 * Represents an obstacle in an agility course.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This class is part of the hybrid patch-based approach.
 */
public class AgilityObstacle {
    private final String name;
    private final int objectId;
    private final String menuAction;
    private final WorldPosition obstaclePosition;
    private final WorldPosition expectedPlayerPosition;
    private final int expectedAnimationId;
    // Future extensibility: coordinates, actions, etc.

    public AgilityObstacle(String name, int objectId, String menuAction, WorldPosition obstaclePosition, WorldPosition expectedPlayerPosition, int expectedAnimationId) {
        this.name = name;
        this.objectId = objectId;
        this.menuAction = menuAction;
        this.obstaclePosition = obstaclePosition;
        this.expectedPlayerPosition = expectedPlayerPosition;
        this.expectedAnimationId = expectedAnimationId;
    }

    public String getName() {
        return name;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getMenuAction() {
        return menuAction;
    }

    public WorldPosition getObstaclePosition() {
        return obstaclePosition;
    }

    public WorldPosition getExpectedPlayerPosition() {
        return expectedPlayerPosition;
    }

    public int getExpectedAnimationId() {
        return expectedAnimationId;
    }
}
