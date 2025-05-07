package com.osrshelper.agent;

import com.osrshelper.agent.ServiceRegistry;
import java.awt.Component;
import java.awt.event.MouseEvent;
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

    public MouseInputService(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.mouseManager = serviceRegistry.get(MouseManager.class);
        this.gameStateProvider = serviceRegistry.get(GameStateProvider.class);
        this.client = serviceRegistry.get(Client.class);
    }

    /**
     * Simulates a mouse click at the given screen coordinates.
     * @param x The x-coordinate on the screen.
     * @param y The y-coordinate on the screen.
     */
    public void clickAt(int x, int y) {
        try {
            // Create a dummy component as the source for the MouseEvent
            Component source = new java.awt.Canvas();
            long when = System.currentTimeMillis();
            int modifiers = 0; // No modifiers
            int clickCount = 1;
            boolean popupTrigger = false;
            int button = MouseEvent.BUTTON1;

            // Create and dispatch mouse pressed event
            MouseEvent pressEvent = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, when, modifiers, x, y, clickCount, popupTrigger, button);
            mouseManager.processMousePressed(pressEvent);

            // Create and dispatch mouse released event
            MouseEvent releaseEvent = new MouseEvent(source, MouseEvent.MOUSE_RELEASED, when, modifiers, x, y, clickCount, popupTrigger, button);
            mouseManager.processMouseReleased(releaseEvent);

            // Create and dispatch mouse clicked event
            MouseEvent clickEvent = new MouseEvent(source, MouseEvent.MOUSE_CLICKED, when, modifiers, x, y, clickCount, popupTrigger, button);
            mouseManager.processMouseClicked(clickEvent);

            System.out.println("[MouseInputService] Simulated mouse click at (" + x + ", " + y + ")");
        } catch (Exception e) {
            System.err.println("[MouseInputService] Failed to simulate mouse click: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simulates a mouse click on a game object by its ID.
     * For now, this is a stub that logs the action.
     * @param id The game object ID to click.
     */
    public void clickGameObject(int id) {
        try {
            WorldPoint objectWorldPoint = gameStateProvider.getObjectWorldPoint(id);
            if (objectWorldPoint == null) {
                System.err.println("[MouseInputService] Could not find object with ID: " + id);
                return;
            }
            LocalPoint localPoint = LocalPoint.fromWorld(client, objectWorldPoint);
            if (localPoint == null) {
                System.err.println("[MouseInputService] Could not convert WorldPoint to LocalPoint for object ID: " + id);
                return;
            }
            Point canvasPoint = Perspective.localToCanvas(client, localPoint, objectWorldPoint.getPlane());
            if (canvasPoint == null) {
                System.err.println("[MouseInputService] Could not convert LocalPoint to canvas coordinates for object ID: " + id);
                return;
            }
            clickAt(canvasPoint.getX(), canvasPoint.getY());
            System.out.println("[MouseInputService] Simulated click on game object with ID: " + id + " at (" + canvasPoint.getX() + ", " + canvasPoint.getY() + ")");
        } catch (Exception e) {
            System.err.println("[MouseInputService] Failed to simulate click on game object: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
