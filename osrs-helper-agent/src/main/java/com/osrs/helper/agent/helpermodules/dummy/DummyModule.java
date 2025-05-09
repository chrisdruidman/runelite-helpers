package com.osrs.helper.agent.helpermodules.dummy;

import com.osrs.helper.agent.helpermodules.AgentModule;

/**
 * Dummy helper module for demonstration/testing.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This module is part of the hybrid patch-based approach.
 */
public class DummyModule implements AgentModule {
    private boolean enabled = false;

    @Override
    public void onEnable() {
        enabled = true;
        System.out.println("DummyModule enabled");
    }

    @Override
    public void onDisable() {
        enabled = false;
        System.out.println("DummyModule disabled");
    }

    @Override
    public String getName() {
        return "Dummy Module";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
