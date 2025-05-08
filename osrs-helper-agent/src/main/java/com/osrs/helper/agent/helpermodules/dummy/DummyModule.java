package com.osrs.helper.agent.helpermodules.dummy;

import com.osrs.helper.agent.helpermodules.AgentModule;

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
