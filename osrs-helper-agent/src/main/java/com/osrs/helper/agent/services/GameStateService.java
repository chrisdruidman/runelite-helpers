package com.osrs.helper.agent.services;

import java.util.logging.Logger;

/**
 * Service for tracking and interacting with game state via the minimal API.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This service is part of the hybrid patch-based approach.
 *
 * No ASM or runtime injection is used here.
 */
public class GameStateService implements AgentService {
    private static final Logger logger = Logger.getLogger("GameStateService");
    private final HookingService hookingService;

    public GameStateService(HookingService hookingService) {
        this.hookingService = hookingService;
    }

    @Override
    public void initialize() {
        logger.info("GameStateService initialized");
        // Register listeners with HookingService to update local state or trigger logic
        hookingService.addPlayerAnimationListener(isAnimating -> {
            logger.fine("[GameStateService] Player animation state changed: " + isAnimating);
            // Optionally, trigger automation or update local cache
        });
        hookingService.addPlayerPositionListener(position -> {
            logger.fine("[GameStateService] Player position changed: " + position);
            // Optionally, trigger automation or update local cache
        });
        hookingService.addObjectPresenceListener(objectId -> {
            logger.fine("[GameStateService] Object presence changed: " + objectId);
            // Optionally, trigger automation or update local cache
        });
    }

    @Override
    public void shutdown() {
        logger.info("GameStateService shutdown");
        // TODO: Clean up hooks or polling
    }

    // Returns the player's current position (to be implemented via HookingService)
    public Object getPlayerPosition() {
        // Use HookingService to get the player's world position via the minimal API only
        return hookingService.getCurrentPlayerPosition();
    }

    // Returns true if the player is currently animating (to be implemented via HookingService)
    public boolean isPlayerAnimating() {
        // Use HookingService to check if the player is currently performing an animation via the minimal API only
        return hookingService.getCurrentPlayerAnimating();
    }

    // Returns true if the specified object is present in the scene (to be implemented via HookingService)
    public boolean isObjectPresent(String objectId) {
        // Use HookingService to check if the object with the given ID is present in the scene
        return hookingService.isObjectPresentNow(objectId);
    }

    // Returns the player's current animation ID (delegates to HookingService)
    public int getCurrentPlayerAnimationId() {
        return hookingService.getCurrentPlayerAnimationId();
    }
}
