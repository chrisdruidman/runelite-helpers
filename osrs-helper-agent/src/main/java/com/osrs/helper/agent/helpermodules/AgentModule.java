package com.osrs.helper.agent.helpermodules;

/**
 * Base interface for all agent modules (e.g., Agility, Combat).
 * Modules should be self-contained and support enable/disable.
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
