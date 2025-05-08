package com.osrshelper.agent;

import java.util.Optional;

/**
 * Service for interacting with RuneLite menu entries programmatically.
 * Allows modules to trigger menu actions (e.g., obstacle interactions) without mouse simulation.
 *
 * This service should be registered in the ServiceRegistry and used by modules such as AgilityModule.
 */
public interface MenuActionService {
    /**
     * Attempts to find and invoke a menu entry action matching the given option and target.
     *
     * @param option The menu option (e.g., "Jump", "Climb").
     * @param target The menu target (e.g., obstacle name).
     * @return true if the action was found and invoked, false otherwise.
     */
    boolean invokeMenuAction(String option, String target);

    /**
     * Finds a menu entry matching the given option and target, if available.
     *
     * @param option The menu option.
     * @param target The menu target.
     * @return Optional describing the menu entry, or empty if not found.
     */
    Optional<Object> findMenuEntry(String option, String target);
}
