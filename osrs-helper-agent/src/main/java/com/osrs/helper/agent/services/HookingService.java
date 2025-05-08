package com.osrs.helper.agent.services;

import java.util.*;
import java.util.logging.Logger;
import java.util.function.Consumer;

/**
 * Service for managing ASM-injected hooks and reflection-based listeners into the RuneLite client.
 * Provides a modular API for subscribing to and querying game state, player events, menu entries, etc.
 * All hook/event logic should be centralized here for maintainability and extensibility.
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

    // --- Static hook entry points for ASM-injected code ---

    // Called by ASM-injected code when the player's animation state changes
    public static void onPlayerAnimationChanged(boolean isAnimating, int animationId) {
        getInstance().setPlayerAnimating(isAnimating);
        getInstance().setCurrentPlayerAnimationId(animationId);
    }

    // Called by ASM-injected code when the player's position changes
    public static void onPlayerPositionChanged(Object position) {
        getInstance().setPlayerPosition(position);
    }

    // Called by ASM-injected code when an object's presence changes
    public static void onObjectPresenceChanged(String objectId, boolean present) {
        getInstance().setObjectPresence(objectId, present);
    }

    // --- Internal state and event dispatch ---
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

    private void setPlayerAnimating(boolean isAnimating) {
        this.currentPlayerAnimating = isAnimating;
        notifyPlayerAnimationChanged(isAnimating);
    }

    private void setCurrentPlayerAnimationId(int animationId) {
        this.currentPlayerAnimationId = animationId;
    }

    public int getCurrentPlayerAnimationId() {
        return currentPlayerAnimationId;
    }

    private void setPlayerPosition(Object position) {
        this.currentPlayerPosition = position;
        notifyPlayerPositionChanged(position);
    }

    private void setObjectPresence(String objectId, boolean present) {
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

    public Object getCurrentPlayerPosition() {
        return currentPlayerPosition;
    }

    public boolean isObjectPresentNow(String objectId) {
        return presentObjects.contains(objectId);
    }

    @Override
    public void initialize() {
        logger.info("HookingService initialized");
        // TODO: Set up ASM hooks or reflection listeners for RuneLite client events
    }

    @Override
    public void shutdown() {
        logger.info("HookingService shutdown");
        // TODO: Clean up hooks/listeners
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

    // TODO: Add methods for querying current state, registering additional hooks, etc.
}
