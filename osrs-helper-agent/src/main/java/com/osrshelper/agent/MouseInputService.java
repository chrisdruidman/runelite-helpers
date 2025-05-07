package com.osrshelper.agent;

import com.osrshelper.agent.ServiceRegistry;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.logging.Logger;
import net.runelite.api.Client;
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
     * @param x The x-coordinate on the screen.
     * @param y The y-coordinate on the screen.
     */
    public void clickAt(int x, int y) {
        logger.info("[MouseInputService] Attempting to simulate mouse click at (" + x + ", " + y + ")");
        try {
            Component source = getGameCanvas();
            long when = System.currentTimeMillis();
            int modifiers = 0; // No modifiers
            int clickCount = 1;
            boolean popupTrigger = false;
            int button = MouseEvent.BUTTON1;

            MouseEvent pressEvent = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, when, modifiers, x, y, clickCount, popupTrigger, button);
            MouseEvent releaseEvent = new MouseEvent(source, MouseEvent.MOUSE_RELEASED, when, modifiers, x, y, clickCount, popupTrigger, button);
            MouseEvent clickEvent = new MouseEvent(source, MouseEvent.MOUSE_CLICKED, when, modifiers, x, y, clickCount, popupTrigger, button);

            // Dispatch directly to the canvas
            logger.info("[MouseInputService] Dispatching mouse events directly to canvas at (" + x + ", " + y + ")");
            source.dispatchEvent(pressEvent);
            source.dispatchEvent(releaseEvent);
            source.dispatchEvent(clickEvent);
            logger.info("[MouseInputService] Dispatched mouse events directly to canvas at (" + x + ", " + y + ")");

            // Also post to the AWT EventQueue and MouseManager for comparison
            java.awt.EventQueue.invokeLater(() -> {
                mouseManager.processMousePressed(pressEvent);
                mouseManager.processMouseReleased(releaseEvent);
                mouseManager.processMouseClicked(clickEvent);
                logger.info("[MouseInputService] Posted mouse click events to AWT EventQueue at (" + x + ", " + y + ")");
            });
        } catch (Exception e) {
            logger.severe("[MouseInputService] Failed to simulate mouse click: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simulates a mouse click on a game object by its ID.
     * For now, this is a stub that logs the action.
     * @param id The game object ID to click.
     */
    public void clickGameObject(int id) {
        logger.info("[MouseInputService] Attempting to click game object with ID: " + id);
        try {
            WorldPoint objectWorldPoint = gameStateProvider.getObjectWorldPoint(id);
            if (objectWorldPoint == null) {
                logger.warning("[MouseInputService] Could not find object with ID: " + id);
                return;
            }
            LocalPoint localPoint = LocalPoint.fromWorld(client, objectWorldPoint);
            if (localPoint == null) {
                logger.warning("[MouseInputService] Could not convert WorldPoint to LocalPoint for object ID: " + id);
                return;
            }
            Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
            if (poly != null && poly.npoints > 0) {
                // Randomize click point within the polygon
                int x, y;
                int attempts = 0;
                do {
                    int minX = poly.getBounds().x;
                    int minY = poly.getBounds().y;
                    int maxX = minX + poly.getBounds().width;
                    int maxY = minY + poly.getBounds().height;
                    x = minX + random.nextInt(Math.max(1, maxX - minX));
                    y = minY + random.nextInt(Math.max(1, maxY - minY));
                    attempts++;
                } while (!poly.contains(x, y) && attempts < 10);
                if (!poly.contains(x, y)) {
                    // Fallback to center if randomization fails
                    x = (int) poly.getBounds2D().getCenterX();
                    y = (int) poly.getBounds2D().getCenterY();
                }
                clickAt(x, y);
                logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + x + ", " + y + ") [randomized in tile polygon]");
            } else {
                // Fallback to previous logic (center of tile)
                Point canvasPoint = Perspective.localToCanvas(client, localPoint, objectWorldPoint.getPlane());
                if (canvasPoint == null) {
                    logger.warning("[MouseInputService] Could not convert LocalPoint to canvas coordinates for object ID: " + id);
                    return;
                }
                clickAt(canvasPoint.getX(), canvasPoint.getY());
                logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + canvasPoint.getX() + ", " + canvasPoint.getY() + ") [fallback center]");
            }
        } catch (Exception e) {
            logger.severe("[MouseInputService] Failed to simulate click on game object: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
