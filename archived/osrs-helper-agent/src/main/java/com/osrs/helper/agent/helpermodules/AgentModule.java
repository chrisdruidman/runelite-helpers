package com.osrs.helper.agent.helpermodules;

/**
 * Base class for all agent helper modules.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. All modules must comply with the hybrid patch-based approach.
 */
public interface AgentModule {
    /**
     * Called when the module is enabled.
     */
    void onEnable();

    /**
     * Called when the module is disabled.
     */
    void onDisable();

    /**
     * @return the display name of the module
     */
    String getName();

    /**
     * @return true if the module is currently enabled
     */
    boolean isEnabled();
}
