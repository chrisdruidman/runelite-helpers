package com.osrs.helper.agent.listeners;

import com.osrs.helper.agent.helpermodules.AgentModule;

/**
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
