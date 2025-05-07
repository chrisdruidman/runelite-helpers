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
     * Simulates a mouse click on a game object by its ID.
     * Uses the object's clickbox if available, matching RuneLite overlay logic.
     * @param id The game object ID to click.
     */
    public void clickGameObject(int id) {
        logger.info("[MouseInputService] Attempting to click game object with ID: " + id);
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
                            // Check GameObjects
                            net.runelite.api.GameObject[] gameObjects = tile.getGameObjects();
                            if (gameObjects != null) {
                                for (net.runelite.api.GameObject obj : gameObjects) {
                                    if (obj != null && obj.getId() == id) {
                                        foundObject = obj;
                                        break outer;
                                    }
                                }
                            }
                            // Check WallObject
                            net.runelite.api.WallObject wall = tile.getWallObject();
                            if (wall != null && wall.getId() == id) {
                                foundObject = wall;
                                break outer;
                            }
                            // Check DecorativeObject
                            net.runelite.api.DecorativeObject deco = tile.getDecorativeObject();
                            if (deco != null && deco.getId() == id) {
                                foundObject = deco;
                                break outer;
                            }
                            // Check GroundObject
                            net.runelite.api.GroundObject ground = tile.getGroundObject();
                            if (ground != null && ground.getId() == id) {
                                foundObject = ground;
                                break outer;
                            }
                        }
                    }
                }
            }
            if (foundObject != null) {
                logger.info("[MouseInputService] Found TileObject for ID " + id + ": " + foundObject.getClass().getSimpleName());
                java.awt.Shape clickbox = null;
                // Use model-based clickbox if possible
                if (foundObject instanceof net.runelite.api.Renderable) {
                    net.runelite.api.Renderable renderable = (net.runelite.api.Renderable) foundObject;
                    net.runelite.api.Model model = renderable.getModel();
                    if (model != null) {
                        LocalPoint lp = foundObject.getLocalLocation();
                        int orientation = 0;
                        try {
                            orientation = (Integer) foundObject.getClass().getMethod("getOrientation").invoke(foundObject);
                        } catch (Exception ignored) {}
                        int z = Perspective.getTileHeight(client, lp, foundObject.getPlane());
                        clickbox = Perspective.getClickbox(client, model, orientation, lp.getX(), lp.getY(), z);
                        if (clickbox != null) {
                            logger.info("[MouseInputService] Using model-based clickbox for " + foundObject.getClass().getSimpleName());
                        }
                    }
                }
                if (clickbox == null) {
                    clickbox = foundObject.getClickbox();
                    logger.info("[MouseInputService] Using fallback getClickbox() for " + foundObject.getClass().getSimpleName());
                }
                lastClickbox = clickbox; // Store for overlay
                if (clickbox != null) {
                    if (clickbox instanceof Polygon) {
                        Polygon poly = (Polygon) clickbox;
                        // Compute centroid
                        double cx = 0, cy = 0, area = 0;
                        for (int i = 0, j = poly.npoints - 1; i < poly.npoints; j = i++) {
                            double temp = poly.xpoints[j] * poly.ypoints[i] - poly.xpoints[i] * poly.ypoints[j];
                            area += temp;
                            cx += (poly.xpoints[j] + poly.xpoints[i]) * temp;
                            cy += (poly.ypoints[j] + poly.ypoints[i]) * temp;
                        }
                        area *= 0.5;
                        int x, y;
                        if (area != 0) {
                            cx /= (6 * area);
                            cy /= (6 * area);
                            // Add a small random offset (max 3px) for humanization and shift left by 5px
                            x = (int) Math.round(cx + random.nextInt(7) - 3 - 5);
                            y = (int) Math.round(cy + random.nextInt(7) - 3);
                        } else {
                            x = (int) poly.getBounds().getCenterX() - 5;
                            y = (int) poly.getBounds().getCenterY();
                        }
                        clickAt(x, y);
                        logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + x + ", " + y + ") [polygon centroid]");
                        return;
                    } else {
                        java.awt.Rectangle bounds = clickbox.getBounds();
                        int x = (int) bounds.getCenterX() + random.nextInt(7) - 3;
                        int y = (int) bounds.getCenterY() + random.nextInt(7) - 3;
                        clickAt(x, y);
                        logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + x + ", " + y + ") [rect center]");
                        return;
                    }
                } else {
                    logger.info("[MouseInputService] Clickbox was null, falling back to tile poly.");
                }
            } else {
                logger.warning("[MouseInputService] Could not find TileObject for ID: " + id + ". Falling back to previous logic.");
            }
            // Fallback: use previous logic (tile poly or localToCanvas)
            WorldPoint objectWorldPoint = gameStateProvider.getObjectWorldPoint(id);
            logger.info("[MouseInputService] objectWorldPoint for ID " + id + ": " + objectWorldPoint);
            if (objectWorldPoint == null) {
                logger.warning("[MouseInputService] Could not find object with ID: " + id);
                return;
            }
            LocalPoint localPoint = LocalPoint.fromWorld(client, objectWorldPoint);
            logger.info("[MouseInputService] localPoint for ID " + id + ": " + localPoint);
            if (localPoint == null) {
                logger.warning("[MouseInputService] Could not convert WorldPoint to LocalPoint for object ID: " + id);
                return;
            }
            Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
            logger.info("[MouseInputService] Polygon for ID " + id + ": " + (poly != null ? poly.getBounds().toString() : "null"));
            if (poly != null && poly.npoints > 0) {
                int x = 0, y = 0;
                int attempts = 0;
                int minX = poly.getBounds().x;
                int minY = poly.getBounds().y;
                int maxX = minX + poly.getBounds().width;
                int maxY = minY + poly.getBounds().height;
                boolean found = false;
                for (attempts = 0; attempts < 20; attempts++) {
                    poly = Perspective.getCanvasTilePoly(client, localPoint);
                    x = minX + random.nextInt(Math.max(1, maxX - minX));
                    y = minY + random.nextInt(Math.max(1, maxY - minY));
                    if (poly.contains(x, y)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Compute geometric centroid of the polygon
                    double cx = 0, cy = 0;
                    double area = 0;
                    for (int i = 0, j = poly.npoints - 1; i < poly.npoints; j = i++) {
                        double temp = poly.xpoints[j] * poly.ypoints[i] - poly.xpoints[i] * poly.ypoints[j];
                        area += temp;
                        cx += (poly.xpoints[j] + poly.xpoints[i]) * temp;
                        cy += (poly.ypoints[j] + poly.ypoints[i]) * temp;
                    }
                    area *= 0.5;
                    if (area != 0) {
                        cx /= (6 * area);
                        cy /= (6 * area);
                        x = (int) Math.round(cx);
                        y = (int) Math.round(cy);
                    } else {
                        int sumX = 0, sumY = 0;
                        for (int i = 0; i < poly.npoints; i++) {
                            sumX += poly.xpoints[i];
                            sumY += poly.ypoints[i];
                        }
                        x = sumX / poly.npoints;
                        y = sumY / poly.npoints;
                    }
                }
                poly = Perspective.getCanvasTilePoly(client, localPoint);
                if (poly != null && poly.contains(x, y)) {
                    clickAt(x, y);
                    logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + x + ", " + y + ") [in tile polygon]");
                } else {
                    Point canvasPoint = Perspective.localToCanvas(client, localPoint, objectWorldPoint.getPlane());
                    logger.warning("[MouseInputService] Final click point not in polygon after re-fetch, using localToCanvas fallback: " + canvasPoint);
                    if (canvasPoint != null) {
                        clickAt(canvasPoint.getX(), canvasPoint.getY());
                        logger.info("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + canvasPoint.getX() + ", " + canvasPoint.getY() + ") [localToCanvas fallback]");
                    }
                }
            } else {
                Point canvasPoint = Perspective.localToCanvas(client, localPoint, objectWorldPoint.getPlane());
                logger.info("[MouseInputService] Fallback Perspective.localToCanvas for ID " + id + ": " + canvasPoint);
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

    public java.awt.Shape getLastClickbox() {
        return lastClickbox;
    }
}