package com.osrs.helper.agent.listeners;

import com.osrs.helper.agent.helpermodules.AgentModule;

/**
 * Listener interface for module toggle events from the overlay.
 */
public interface ModuleToggleListener {
    /**
     * Called when a module is toggled in the overlay.
     * @param module The module being toggled
     * @param enabled True if the module should be enabled, false if disabled
     */
    void onModuleToggled(AgentModule module, boolean enabled);
}
