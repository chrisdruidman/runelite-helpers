package com.osrs.helper.agent.services;

import java.util.*;
import java.util.logging.Logger;
import java.util.function.Consumer;

/**
 * Service for managing hooks and agent integration.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This service is part of the hybrid patch-based approach.
 *
 * No ASM or runtime injection is used here.
 */
public class HookingService implements AgentService {
    private static final Logger logger = Logger.getLogger("HookingService");

    // Example: Listeners for player animation state changes
    private final List<Consumer<Boolean>> playerAnimationListeners = new ArrayList<>();
    // Example: Listeners for player position changes
    private final List<Consumer<Object>> playerPositionListeners = new ArrayList<>();
    // Example: Listeners for object presence changes
    private final List<Consumer<String>> objectPresenceListeners = new ArrayList<>();

    private int currentPlayerAnimationId = -1;
    private static HookingService instance;
    private boolean currentPlayerAnimating = false;
    private Object currentPlayerPosition = null;
    private final Set<String> presentObjects = new HashSet<>();

    public HookingService() {
        instance = this;
    }

    public static HookingService getInstance() {
        return instance;
    }

    // State update methods (to be called by minimal API, not ASM)
    public void setPlayerAnimating(boolean isAnimating) {
        this.currentPlayerAnimating = isAnimating;
        notifyPlayerAnimationChanged(isAnimating);
    }

    public void setCurrentPlayerAnimationId(int animationId) {
        this.currentPlayerAnimationId = animationId;
    }

    public int getCurrentPlayerAnimationId() {
        return currentPlayerAnimationId;
    }

    public void setPlayerPosition(Object position) {
        logger.info("[DEBUG] setPlayerPosition called with: " + (position == null ? "null" : position.toString() + " (" + (position == null ? "null" : position.getClass().getName()) + ")"));
        this.currentPlayerPosition = position;
        notifyPlayerPositionChanged(position);
    }

    public Object getCurrentPlayerPosition() {
        logger.info("[DEBUG] getCurrentPlayerPosition returning: " + (currentPlayerPosition == null ? "null" : currentPlayerPosition.toString() + " (" + currentPlayerPosition.getClass().getName() + ")"));
        return currentPlayerPosition;
    }

    public void setObjectPresence(String objectId, boolean present) {
        if (present) {
            presentObjects.add(objectId);
        } else {
            presentObjects.remove(objectId);
        }
        notifyObjectPresenceChanged(objectId);
    }

    // --- Synchronous state queries for services ---
    public boolean getCurrentPlayerAnimating() {
        return currentPlayerAnimating;
    }

    public boolean isObjectPresentNow(String objectId) {
        return presentObjects.contains(objectId);
    }

    @Override
    public void initialize() {
        logger.info("HookingService initialized");
        // TODO: Set up listeners for RuneLite client events using only the minimal API exposed by patch files
    }

    @Override
    public void shutdown() {
        logger.info("HookingService shutdown");
        // TODO: Clean up listeners
    }

    // Register a listener for player animation state changes
    public void addPlayerAnimationListener(Consumer<Boolean> listener) {
        playerAnimationListeners.add(listener);
    }

    // Register a listener for player position changes
    public void addPlayerPositionListener(Consumer<Object> listener) {
        playerPositionListeners.add(listener);
    }

    // Register a listener for object presence changes
    public void addObjectPresenceListener(Consumer<String> listener) {
        objectPresenceListeners.add(listener);
    }

    // Example: Notify all listeners of a player animation state change
    public void notifyPlayerAnimationChanged(boolean isAnimating) {
        for (Consumer<Boolean> listener : playerAnimationListeners) {
            listener.accept(isAnimating);
        }
    }

    // Example: Notify all listeners of a player position change
    public void notifyPlayerPositionChanged(Object position) {
        for (Consumer<Object> listener : playerPositionListeners) {
            listener.accept(position);
        }
    }

    // Example: Notify all listeners of an object presence change
    public void notifyObjectPresenceChanged(String objectId) {
        for (Consumer<String> listener : objectPresenceListeners) {
            listener.accept(objectId);
        }
    }

    // TODO: Add methods for querying current state, registering additional listeners, etc. All must use the minimal API only.
}
