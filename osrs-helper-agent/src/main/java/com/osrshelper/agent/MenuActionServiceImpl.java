package com.osrshelper.agent;

import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.GameObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.Text;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of MenuActionService for programmatic menu entry interaction.
 * Uses agent instrumentation to access and invoke RuneLite menu entries.
 */
public class MenuActionServiceImpl implements MenuActionService {
    private static final Logger logger = Logger.getLogger(MenuActionServiceImpl.class.getName());
    private final Client client;
    private final ServiceRegistry serviceRegistry;

    public MenuActionServiceImpl(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.client = serviceRegistry.get(Client.class);
    }

    @Override
    public boolean invokeMenuAction(String option, String target) {
        // Try to find the menu entry first
        Optional<MenuEntry> entryOpt = findMenuEntry(option, target).map(o -> (MenuEntry) o);
        
        if (entryOpt.isPresent()) {
            MenuEntry entry = entryOpt.get();
            try {
                if (entry.onClick() != null) {
                    logger.info("Invoking onClick callback for menu entry: " + option + " | " + target);
                    entry.onClick().accept(entry);
                    return true;
                } else {
                    // Fallback: simulate click by calling client.menuAction (reflection)
                    logger.info("No onClick callback, attempting to invoke menu action via reflection for: " + option + " | " + target);
                    Method menuActionMethod = client.getClass().getMethod(
                        "menuAction",
                        int.class, int.class, int.class, int.class, String.class, String.class
                    );
                    menuActionMethod.invoke(
                        client,
                        entry.getParam0(),
                        entry.getParam1(),
                        entry.getType().getId(),
                        entry.getIdentifier(),
                        entry.getOption(),
                        entry.getTarget()
                    );
                    return true;
                }
            } catch (Exception e) {
                logger.severe("Failed to invoke menu action: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // If menu entry not found, try to interact with the GameObject directly
            logger.info("Menu entry not found, trying to directly interact with object");
            return interactWithGameObjectById(option, target);
        }
        return false;
    }

    /**
     * Try to interact with a GameObject by finding it by ID and interacting with it directly
     */
    private boolean interactWithGameObjectById(String option, String target) {
        if (client == null) {
            logger.severe("Client instance is null");
            return false;
        }

        // Get the obstacle ID from the current course
        int obstacleId = -1;
        AgilityCourse currentCourse = getCurrentCourse();
        if (currentCourse != null) {
            obstacleId = currentCourse.getNextObstacleId();
        }

        if (obstacleId == -1) {
            logger.warning("No obstacle ID found for interaction");
            return false;
        }

        logger.info("Trying to interact with obstacle ID: " + obstacleId);

        // Find the GameObject by ID
        GameObject targetObject = findGameObjectById(obstacleId);
        if (targetObject == null) {
            logAllGameObjects(); // Debug log all game objects
            logger.warning("Could not find game object with ID: " + obstacleId);
            return false;
        }

        // Interact with the GameObject
        logger.info("Found game object, interacting with it: " + targetObject.getId());
        try {
            // Use reflection to interact with the object
            LocalPoint localLocation = targetObject.getLocalLocation();
            
            // Use the correct menuAction method found in the RuneLite Client API
            Method menuActionMethod = client.getClass().getMethod(
                "menuAction",
                int.class, int.class, net.runelite.api.MenuAction.class, int.class, int.class, String.class, String.class
            );
            
            // Try to get action parameters from object
            int param0 = 0; // Menu action parameter 0 
            int param1 = 0; // Menu action parameter 1
            net.runelite.api.MenuAction actionType = net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION; // Default to first option
            int id = targetObject.getId();
            int itemId = -1; // Not an item
            String menuOption = option;
            String menuTarget = target;
            
            logger.info("Invoking menuAction with params: " + 
                      "param0=" + param0 + 
                      ", param1=" + param1 + 
                      ", actionType=" + actionType + 
                      ", id=" + id + 
                      ", itemId=" + itemId + 
                      ", option='" + menuOption + "'" + 
                      ", target='" + menuTarget + "'");
            
            menuActionMethod.invoke(
                client,
                param0,
                param1,
                actionType,
                id,
                itemId,
                menuOption,
                menuTarget
            );
            
            return true;
        } catch (Exception e) {
            logger.severe("Failed to interact with game object: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find a GameObject by its ID
     */
    private GameObject findGameObjectById(int id) {
        if (client == null) {
            return null;
        }

        Scene scene = client.getScene();
        if (scene == null) {
            return null;
        }

        Tile[][][] tiles = scene.getTiles();
        if (tiles == null) {
            return null;
        }

        // Search through all tiles in the scene
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < tiles[z].length; x++) {
                for (int y = 0; y < tiles[z][x].length; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile == null) {
                        continue;
                    }

                    // Check game objects on tile
                    GameObject[] gameObjects = tile.getGameObjects();
                    if (gameObjects != null) {
                        for (GameObject obj : gameObjects) {
                            if (obj != null && obj.getId() == id) {
                                logger.info("Found game object with ID " + id + " at position (" + x + ", " + y + ", " + z + ")");
                                return obj;
                            }
                        }
                    }

                    // Check wall objects
                    if (tile.getWallObject() != null && tile.getWallObject().getId() == id) {
                        logger.info("Found wall object with ID " + id + " at position (" + x + ", " + y + ", " + z + ")");
                        return (GameObject) tile.getWallObject();
                    }

                    // Check decorative objects
                    if (tile.getDecorativeObject() != null && tile.getDecorativeObject().getId() == id) {
                        logger.info("Found decorative object with ID " + id + " at position (" + x + ", " + y + ", " + z + ")");
                        return (GameObject) tile.getDecorativeObject();
                    }

                    // Check ground objects
                    if (tile.getGroundObject() != null && tile.getGroundObject().getId() == id) {
                        logger.info("Found ground object with ID " + id + " at position (" + x + ", " + y + ", " + z + ")");
                        return (GameObject) tile.getGroundObject();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Log all game objects for debugging
     */
    private void logAllGameObjects() {
        if (client == null) {
            return;
        }

        Scene scene = client.getScene();
        if (scene == null) {
            return;
        }

        Tile[][][] tiles = scene.getTiles();
        if (tiles == null) {
            return;
        }

        logger.info("DEBUG: All visible game objects:");
        int objCount = 0;

        // Get player location for reference
        LocalPoint playerLocation = null;
        if (client.getLocalPlayer() != null) {
            playerLocation = client.getLocalPlayer().getLocalLocation();
        }

        // Search through tiles near player (to limit debug output)
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < tiles[z].length; x++) {
                for (int y = 0; y < tiles[z][x].length; y++) {
                    Tile tile = tiles[z][x][y];
                    if (tile == null) {
                        continue;
                    }

                    // Check if near player (within 30 tiles)
                    if (playerLocation != null) {
                        LocalPoint tileLocation = tile.getLocalLocation();
                        if (tileLocation != null) {
                            int distance = Math.abs(tileLocation.getX() - playerLocation.getX()) + 
                                          Math.abs(tileLocation.getY() - playerLocation.getY());
                            if (distance > 3000) { // 30 tiles * 100 units per tile
                                continue; // Skip if too far from player
                            }
                        }
                    }

                    // Check game objects
                    GameObject[] gameObjects = tile.getGameObjects();
                    if (gameObjects != null) {
                        for (GameObject obj : gameObjects) {
                            if (obj != null) {
                                logger.info("DEBUG: GameObject[" + (objCount++) + "]: ID=" + obj.getId() + 
                                          " at (" + x + "," + y + "," + z + ")");
                            }
                        }
                    }

                    // Check wall objects
                    if (tile.getWallObject() != null) {
                        logger.info("DEBUG: WallObject[" + (objCount++) + "]: ID=" + tile.getWallObject().getId() + 
                                  " at (" + x + "," + y + "," + z + ")");
                    }

                    // Check decorative objects
                    if (tile.getDecorativeObject() != null) {
                        logger.info("DEBUG: DecorativeObject[" + (objCount++) + "]: ID=" + tile.getDecorativeObject().getId() + 
                                  " at (" + x + "," + y + "," + z + ")");
                    }

                    // Check ground objects
                    if (tile.getGroundObject() != null) {
                        logger.info("DEBUG: GroundObject[" + (objCount++) + "]: ID=" + tile.getGroundObject().getId() + 
                                  " at (" + x + "," + y + "," + z + ")");
                    }
                }
            }
        }

        logger.info("DEBUG: Found " + objCount + " game objects");
    }

    /**
     * Get the current agility course from the service registry
     */
    private AgilityCourse getCurrentCourse() {
        // Try to get the AgilityModule
        AgilityModule module = serviceRegistry.get(AgilityModule.class);
        if (module != null) {
            AgilityCourse course = module.getActiveCourse();
            if (course != null) {
                return course;
            } else {
                logger.info("AgilityModule found in registry but getActiveCourse() returned null");
            }
        } else {
            logger.warning("AgilityModule not found in ServiceRegistry");
        }
        return null;
    }

    /**
     * Log all available menu entries to help debug matching issues
     */
    private void logAllMenuEntries(String searchOption, String searchTarget) {
        if (client == null || client.getMenuEntries() == null || client.getMenuEntries().length == 0) {
            logger.info("DEBUG: No menu entries available");
            return;
        }
        
        logger.info("DEBUG: Available menu entries while searching for '" + searchOption + " | " + searchTarget + "':");
        MenuEntry[] entries = client.getMenuEntries();
        for (int i = 0; i < entries.length; i++) {
            MenuEntry entry = entries[i];
            if (entry == null || entry.getOption() == null || entry.getTarget() == null) {
                logger.info("DEBUG: Menu[" + i + "]: NULL or has null components");
                continue;
            }
            logger.info("DEBUG: Menu[" + i + "]: '" + entry.getOption() + "' | '" + entry.getTarget() + 
                    "' (Type: " + entry.getType() + ", ID: " + entry.getIdentifier() + ")");
        }
    }

    public Optional<Object> findMenuEntry(String option, String target, Object actionType) {
        if (client == null) {
            logger.severe("Client instance is null in MenuActionServiceImpl");
            return Optional.empty();
        }
        
        MenuEntry[] entries = client.getMenuEntries();
        if (entries == null || entries.length == 0) {
            logger.info("Menu entries array is empty or null");
            return Optional.empty();
        }
        
        String optionNorm = Text.removeTags(option).toLowerCase();
        String targetNorm = Text.removeTags(target).toLowerCase();
        
        for (MenuEntry entry : entries) {
            if (entry == null || entry.getOption() == null || entry.getTarget() == null) {
                continue; // Skip entries with null option/target
            }
            
            String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
            String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
            
            boolean typeMatches = true;
            if (actionType != null) {
                typeMatches = entry.getType().equals(actionType);
            }
            
            // Allow partial matches: option/target must be contained in the entry (or vice versa)
            boolean optionMatch = entryOption.contains(optionNorm) || optionNorm.contains(entryOption);
            boolean targetMatch = entryTarget.contains(targetNorm) || targetNorm.contains(entryTarget);
            
            if (optionMatch && targetMatch && typeMatches) {
                return Optional.of(entry);
            }
        }
        
        // Enhanced debug logging to show all available menu entries
        logAllMenuEntries(option, target);
        
        return Optional.empty();
    }

    @Override
    public Optional<Object> findMenuEntry(String option, String target) {
        return findMenuEntry(option, target, null);
    }
}
