package com.osrs.helper.agent.listeners;

import com.osrs.helper.agent.helpermodules.AgentModule;

/**
 * Listener for module toggle events.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This listener is part of the hybrid patch-based approach.
 */
public interface ModuleToggleListener {
    /**
     * Called when a module is toggled in the overlay.
     * @param module The module being toggled
     * @param enabled True if the module should be enabled, false if disabled
     */
    void onModuleToggled(AgentModule module, boolean enabled);
}
