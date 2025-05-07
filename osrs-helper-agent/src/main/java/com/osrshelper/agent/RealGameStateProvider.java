package com.osrshelper.agent;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RealGameStateProvider implements GameStateProvider {
    private static final Logger logger = Logger.getLogger("RealGameStateProvider");
    private final Client client;

    public RealGameStateProvider(Client client) {
        this.client = client;
    }

    @Override
    public int[] getNearbyObstacleIds() {
        try {
            Scene scene = client.getScene();
            if (scene == null) return new int[0];
            Set<Integer> foundIds = new HashSet<>();
            // Scan all tiles in the scene for game objects
            for (int x = 0; x < scene.getTiles().length; x++) {
                for (int y = 0; y < scene.getTiles()[x].length; y++) {
                    for (int z = 0; z < scene.getTiles()[x][y].length; z++) {
                        Tile tile = scene.getTiles()[x][y][z];
                        if (tile == null) continue;
                        GameObject[] gameObjects = tile.getGameObjects();
                        if (gameObjects == null) continue;
                        for (GameObject obj : gameObjects) {
                            if (obj == null) continue;
                            int id = obj.getId();
                            foundIds.add(id);
                        }
                    }
                }
            }
            return foundIds.stream().mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get nearby obstacle IDs", e);
        }
        return new int[0];
    }

    @Override
    public WorldPoint getPlayerWorldPoint() {
        try {
            Player player = client.getLocalPlayer();
            if (player != null) {
                return player.getWorldLocation();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get player world point", e);
        }
        return null;
    }
}
