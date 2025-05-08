package com.osrs.helper.agent.helpermodules.agility;

import java.util.List;
import java.util.Arrays;

/**
 * Implementation of the Canifis rooftop agility course.
 */
public class CanifisCourse implements AgilityCourse {
    private final List<AgilityObstacle> obstacles = Arrays.asList(
        new AgilityObstacle("Start Tree", "ROOFTOPS_CANIFIS_START_TREE"),
        new AgilityObstacle("Gap 1", "ROOFTOPS_CANIFIS_JUMP"),
        new AgilityObstacle("Gap 2", "ROOFTOPS_CANIFIS_JUMP_2"),
        new AgilityObstacle("Gap 3", "ROOFTOPS_CANIFIS_JUMP_5"),
        new AgilityObstacle("Gap 4", "ROOFTOPS_CANIFIS_JUMP_3"),
        new AgilityObstacle("Pole Vault", "ROOFTOPS_CANIFIS_POLEVAULT"),
        new AgilityObstacle("Gap 5", "ROOFTOPS_CANIFIS_JUMP_4"),
        new AgilityObstacle("Leap Down (Finish)", "ROOFTOPS_CANIFIS_LEAPDOWN")
    );

    @Override
    public String getName() {
        return "Canifis Rooftop";
    }

    public List<AgilityObstacle> getObstacles() {
        return obstacles;
    }
}
