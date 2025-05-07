package com.osrshelper.agent;

import com.osrshelper.agent.ServiceRegistry;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.logging.Logger;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.client.input.MouseManager;

/**
 * Provides methods to simulate mouse input for automation modules.
 * This service should be used by modules (e.g., AgilityModule) to perform mouse clicks.
 *
 * NOTE: Actual implementation of input injection will be added later.
 */
public class MouseInputService {
    private final MouseManager mouseManager;
    private final GameStateProvider gameStateProvider;
    private final Client client;
    private final ServiceRegistry serviceRegistry;
    private static final Logger logger = Logger.getLogger("AgentLogger");
    private Component gameCanvas = null;
    private final Random random = new Random();
    private volatile java.awt.Shape lastClickbox = null;

    public MouseInputService(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.mouseManager = serviceRegistry.get(MouseManager.class);
        this.gameStateProvider = serviceRegistry.get(GameStateProvider.class);
        this.client = serviceRegistry.get(Client.class);
    }

    private Component findGameCanvasRecursive(Component comp) {
        if (comp instanceof java.awt.Canvas) {
            logger.info("[MouseInputService] Found canvas: " + comp.getClass().getName());
            return comp;
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                Component found = findGameCanvasRecursive(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Component getGameCanvas() {
        if (gameCanvas != null) return gameCanvas;
        try {
            // Try to find the RuneLite main window and its canvas recursively
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                Component found = findGameCanvasRecursive(frame);
                if (found != null) {
                    gameCanvas = found;
                    break;
                }
            }
            if (gameCanvas == null) {
                logger.warning("[MouseInputService] Could not find game canvas, using fallback");
            }
        } catch (Exception e) {
            logger.warning("[MouseInputService] Failed to find game canvas: " + e.getMessage());
        }
        return gameCanvas != null ? gameCanvas : new java.awt.Canvas(); // fallback
    }

    /**
     * Simulates a mouse click at the given screen coordinates.
     * @param x The x-coordinate on the screen (canvas coordinates).
     * @param y The y-coordinate on the screen (canvas coordinates).
     */
    public void clickAt(int x, int y) {
        logger.info("[MouseInputService] Attempting to simulate mouse click at (" + x + ", " + y + ")");
        try {
            Component source = getGameCanvas();
            // Debug: log canvas size and location
            java.awt.Point canvasLoc = source.getLocationOnScreen();
            logger.info("[MouseInputService] Canvas location on screen: " + canvasLoc + ", size: " + source.getWidth() + "x" + source.getHeight());
            // Debug: log client stretched mode
            java.awt.Dimension stretched = client.getStretchedDimensions();
            java.awt.Dimension real = client.getRealDimensions();
            logger.info("[MouseInputService] Client stretched dimensions: " + stretched + ", real dimensions: " + real);
            int tempX = x;
            int tempY = y;
            if (!stretched.equals(real)) {
                tempX = (int) (x * (stretched.getWidth() / real.getWidth()));
                tempY = (int) (y * (stretched.getHeight() / real.getHeight()));
                logger.info("[MouseInputService] Adjusted for stretched mode: (" + tempX + ", " + tempY + ")");
            }
            final int clickX = tempX;
            final int clickY = tempY;
            long when = System.currentTimeMillis();
            int modifiers = 0; // No modifiers
            int clickCount = 1;
            boolean popupTrigger = false;
            int button = MouseEvent.BUTTON1;
            final MouseEvent pressEvent = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, when, modifiers, clickX, clickY, clickCount, popupTrigger, button);
            final MouseEvent releaseEvent = new MouseEvent(source, MouseEvent.MOUSE_RELEASED, when, modifiers, clickX, clickY, clickCount, popupTrigger, button);
            final MouseEvent clickEvent = new MouseEvent(source, MouseEvent.MOUSE_CLICKED, when, modifiers, clickX, clickY, clickCount, popupTrigger, button);
            logger.info("[MouseInputService] Dispatching mouse events directly to canvas at (" + clickX + ", " + clickY + ")");
            source.dispatchEvent(pressEvent);
            source.dispatchEvent(releaseEvent);
            source.dispatchEvent(clickEvent);
            logger.info("[MouseInputService] Dispatched mouse events directly to canvas at (" + clickX + ", " + clickY + ")");
            // Also post to the AWT EventQueue and MouseManager for comparison
            java.awt.EventQueue.invokeLater(() -> {
                mouseManager.processMousePressed(pressEvent);
                mouseManager.processMouseReleased(releaseEvent);
                mouseManager.processMouseClicked(clickEvent);
                logger.info("[MouseInputService] Posted mouse click events to AWT EventQueue at (" + clickX + ", " + clickY + ")");
            });
        } catch (Exception e) {
            logger.severe("[MouseInputService] Failed to simulate mouse click: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Interacts with a game object by programmatically creating a menu entry and invoking the correct menu action using reflection.
     * @param id The game object ID to interact with.
     */
    public void clickGameObject(int id) {
        logger.info("[MouseInputService] Attempting to interact with game object via forced menu entry for ID: " + id);
        try {
            // Find the TileObject (GameObject, WallObject, DecorativeObject, GroundObject) with the given ID
            net.runelite.api.TileObject foundObject = null;
            net.runelite.api.Scene scene = client.getScene();
            if (scene != null) {
                net.runelite.api.Tile[][][] tiles = scene.getTiles();
                outer:
                for (int z = 0; z < tiles.length; z++) {
                    for (int x = 0; x < tiles[z].length; x++) {
                        for (int y = 0; y < tiles[z][x].length; y++) {
                            net.runelite.api.Tile tile = tiles[z][x][y];
                            if (tile == null) continue;
                            net.runelite.api.GameObject[] gameObjects = tile.getGameObjects();
                            if (gameObjects != null) {
                                for (net.runelite.api.GameObject obj : gameObjects) {
                                    if (obj != null && obj.getId() == id) {
                                        foundObject = obj;
                                        break outer;
                                    }
                                }
                            }
                            net.runelite.api.WallObject wall = tile.getWallObject();
                            if (wall != null && wall.getId() == id) {
                                foundObject = wall;
                                break outer;
                            }
                            net.runelite.api.DecorativeObject deco = tile.getDecorativeObject();
                            if (deco != null && deco.getId() == id) {
                                foundObject = deco;
                                break outer;
                            }
                            net.runelite.api.GroundObject ground = tile.getGroundObject();
                            if (ground != null && ground.getId() == id) {
                                foundObject = ground;
                                break outer;
                            }
                        }
                    }
                }
            }
            if (foundObject == null) {
                logger.warning("[MouseInputService] Could not find TileObject for ID: " + id);
                return;
            }
            // Use object's name as target, and default to "Jump" as the action (option)
            String target = foundObject.getName();
            String option = "Jump"; // You can adjust this per obstacle type
            int param0 = foundObject.getLocalLocation().getSceneX();
            int param1 = foundObject.getLocalLocation().getSceneY();
            int identifier = id;
            int opcode = MenuAction.GAME_OBJECT_FIRST_OPTION.getId();
            // Create the menu entry
            try {
                java.lang.reflect.Method createMenuEntry = client.getClass().getMethod("createMenuEntry", int.class);
                Object menuEntry = createMenuEntry.invoke(client, 1); // 1 = number of entries to add
                // Set menu entry fields via reflection
                java.lang.reflect.Method setOption = menuEntry.getClass().getMethod("setOption", String.class);
                java.lang.reflect.Method setTarget = menuEntry.getClass().getMethod("setTarget", String.class);
                java.lang.reflect.Method setIdentifier = menuEntry.getClass().getMethod("setIdentifier", int.class);
                java.lang.reflect.Method setType = menuEntry.getClass().getMethod("setType", int.class);
                java.lang.reflect.Method setParam0 = menuEntry.getClass().getMethod("setParam0", int.class);
                java.lang.reflect.Method setParam1 = menuEntry.getClass().getMethod("setParam1", int.class);
                setOption.invoke(menuEntry, option);
                setTarget.invoke(menuEntry, target);
                setIdentifier.invoke(menuEntry, identifier);
                setType.invoke(menuEntry, opcode);
                setParam0.invoke(menuEntry, param0);
                setParam1.invoke(menuEntry, param1);
                logger.info("[MouseInputService] Created menu entry for object ID: " + id + ", option: " + option + ", target: " + target);
                // Now invoke menuAction
                java.lang.reflect.Method menuAction = client.getClass().getDeclaredMethod(
                    "menuAction",
                    int.class, int.class, int.class, int.class, String.class, String.class, int.class, int.class
                );
                menuAction.setAccessible(true);
                menuAction.invoke(
                    client,
                    param0,
                    param1,
                    opcode,
                    identifier,
                    option,
                    target,
                    0, // canvasX
                    0  // canvasY
                );
                logger.info("[MouseInputService] Invoked menuAction via reflection for object ID: " + id);
            } catch (Exception e) {
                logger.severe("[MouseInputService] Reflection failed to create/invoke menu entry: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.severe("[MouseInputService] Failed to interact with game object via forced menu entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public java.awt.Shape getLastClickbox() {
        return lastClickbox;
    }
}