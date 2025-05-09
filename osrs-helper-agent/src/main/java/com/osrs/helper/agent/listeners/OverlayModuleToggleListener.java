package com.osrs.helper.agent.listeners;

import com.osrs.helper.agent.helpermodules.AgentModule;

/**
 * Listener for overlay module toggle events.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This listener is part of the hybrid patch-based approach.
 * Handles enabling/disabling modules when toggled from the overlay.
 */
public class OverlayModuleToggleListener implements ModuleToggleListener {
    @Override
    public void onModuleToggled(AgentModule module, boolean enabled) {
        if (enabled && !module.isEnabled()) {
            module.onEnable();
        } else if (!enabled && module.isEnabled()) {
            module.onDisable();
        }
    }
}
