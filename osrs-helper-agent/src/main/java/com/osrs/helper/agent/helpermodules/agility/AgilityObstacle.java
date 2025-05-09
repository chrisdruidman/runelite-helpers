package com.osrs.helper.agent.helpermodules.agility;

import com.osrs.helper.agent.helpermodules.agility.WorldPosition;

/**
 * Represents a single obstacle in a rooftop agility course.
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
