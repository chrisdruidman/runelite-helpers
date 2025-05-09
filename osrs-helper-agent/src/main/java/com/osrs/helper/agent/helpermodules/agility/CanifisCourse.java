package com.osrs.helper.agent.helpermodules.agility;

import java.util.List;
import java.util.Arrays;

/**
 * Implementation of the Canifis rooftop agility course.
 */
public class CanifisCourse implements AgilityCourse {
    // Local object ID constants for Canifis rooftop course obstacles (from RuneLite ObjectID)
    private static final int ROOFTOPS_CANIFIS_START_TREE = 10819;
    private static final int ROOFTOPS_CANIFIS_JUMP = 10820;
    private static final int ROOFTOPS_CANIFIS_JUMP_2 = 10821;
    private static final int ROOFTOPS_CANIFIS_JUMP_5 = 10828;
    private static final int ROOFTOPS_CANIFIS_JUMP_3 = 10822;
    private static final int ROOFTOPS_CANIFIS_POLEVAULT = 10823;
    private static final int ROOFTOPS_CANIFIS_JUMP_4 = 10829;
    private static final int ROOFTOPS_CANIFIS_LEAPDOWN = 10831;

    private final List<AgilityObstacle> obstacles = Arrays.asList(
        new AgilityObstacle("Start Tree", ROOFTOPS_CANIFIS_START_TREE, "Climb", new WorldPosition(3508, 3488, 0), new WorldPosition(3508, 3491, 2), 828),
        new AgilityObstacle("Gap 1", ROOFTOPS_CANIFIS_JUMP, "Jump", new WorldPosition(3508, 3492, 2), new WorldPosition(3516, 3492, 2), 828),
        new AgilityObstacle("Gap 2", ROOFTOPS_CANIFIS_JUMP_2, "Jump", new WorldPosition(3516, 3492, 2), new WorldPosition(3523, 3498, 2), 828),
        new AgilityObstacle("Gap 3", ROOFTOPS_CANIFIS_JUMP_5, "Jump", new WorldPosition(3523, 3498, 2), new WorldPosition(3523, 3507, 2), 828),
        new AgilityObstacle("Gap 4", ROOFTOPS_CANIFIS_JUMP_3, "Jump", new WorldPosition(3523, 3507, 2), new WorldPosition(3516, 3513, 2), 828),
        new AgilityObstacle("Pole Vault", ROOFTOPS_CANIFIS_POLEVAULT, "Vault", new WorldPosition(3516, 3513, 2), new WorldPosition(3508, 3513, 2), 11789),
        new AgilityObstacle("Gap 5", ROOFTOPS_CANIFIS_JUMP_4, "Jump", new WorldPosition(3508, 3513, 2), new WorldPosition(3508, 3505, 2), 828),
        new AgilityObstacle("Leap Down (Finish)", ROOFTOPS_CANIFIS_LEAPDOWN, "Leap-down", new WorldPosition(3508, 3505, 2), new WorldPosition(3506, 3504, 0), 832)
    );

    @Override
    public String getName() {
        return "Canifis Rooftop";
    }

    public List<AgilityObstacle> getObstacles() {
        return obstacles;
    }
}
