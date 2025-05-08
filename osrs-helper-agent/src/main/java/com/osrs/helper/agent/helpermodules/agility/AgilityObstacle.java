package com.osrs.helper.agent.helpermodules.agility;

/**
 * Represents a single obstacle in a rooftop agility course.
 */
public class AgilityObstacle {
    private final String name;
    private final String objectId;
    // Future extensibility: coordinates, actions, etc.

    public AgilityObstacle(String name, String objectId) {
        this.name = name;
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public String getObjectId() {
        return objectId;
    }
}
