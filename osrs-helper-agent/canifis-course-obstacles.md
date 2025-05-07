# Canifis Rooftop Agility Course Obstacles

This file documents the obstacle order, object IDs, and coordinates (to be filled in) for the Canifis Rooftop Agility Course. This information is useful for automation and reference.

## Obstacle Order and Object IDs

1. Start Tree - `ROOFTOPS_CANIFIS_START_TREE`
2. Gap 1 - `ROOFTOPS_CANIFIS_JUMP`
3. Gap 2 - `ROOFTOPS_CANIFIS_JUMP_2`
4. Gap 3 - `ROOFTOPS_CANIFIS_JUMP_5`
5. Gap 4 - `ROOFTOPS_CANIFIS_JUMP_3`
6. Pole Vault - `ROOFTOPS_CANIFIS_POLEVAULT`
7. Gap 5 - `ROOFTOPS_CANIFIS_JUMP_4`
8. Leap Down (Finish) - `ROOFTOPS_CANIFIS_LEAPDOWN`

## Coordinates (to be filled in)

| Step | Obstacle Name      | Object ID                   | Coordinate (WorldPoint) |
| ---- | ------------------ | --------------------------- | ----------------------- |
| 1    | Start Tree         | ROOFTOPS_CANIFIS_START_TREE |                         |
| 2    | Gap 1              | ROOFTOPS_CANIFIS_JUMP       |                         |
| 3    | Gap 2              | ROOFTOPS_CANIFIS_JUMP_2     |                         |
| 4    | Gap 3              | ROOFTOPS_CANIFIS_JUMP_5     |                         |
| 5    | Gap 4              | ROOFTOPS_CANIFIS_JUMP_3     |                         |
| 6    | Pole Vault         | ROOFTOPS_CANIFIS_POLEVAULT  |                         |
| 7    | Gap 5              | ROOFTOPS_CANIFIS_JUMP_4     |                         |
| 8    | Leap Down (Finish) | ROOFTOPS_CANIFIS_LEAPDOWN   |                         |

> Fill in coordinates as they are discovered. Object IDs are from RuneLite Obstacles.java.

---

## How RuneLite's AgilityPlugin Uses Obstacle IDs

-   The AgilityPlugin listens for object spawn events (GameObjectSpawned, WallObjectSpawned, DecorativeObjectSpawned, etc.).
-   In the `onTileObject` method, it checks if the spawned object's ID is in `Obstacles.OBSTACLE_IDS` (which includes all Canifis rooftop obstacle IDs).
-   If it matches, the plugin adds the object to its internal `obstacles` map, associating it with a Tile and (optionally) an AgilityShortcut.
-   This allows overlays and other plugin features to highlight, interact with, or track these obstacles.

**Summary:**
The AgilityPlugin will automatically recognize and process the Canifis rooftop course obstacles using the IDs above. This means your automation or helper logic can hook into the same detection mechanism or use similar logic for identifying obstacles in the course.
