package com.osrs.helper.agent;

import com.osrs.helper.agent.services.*;
import com.osrs.helper.agent.helpermodules.*;
import java.util.ArrayList;
import java.util.List;
import com.osrs.helper.agent.services.AgentService;
import com.osrs.helper.agent.helpermodules.AgentModule;
import com.osrs.helper.agent.services.DebugLoggingService;
import com.osrs.helper.agent.helpermodules.dummy.DummyModule;
import com.osrs.helper.agent.listeners.OverlayModuleToggleListener;

public class AgentRegistry {
    private final List<AgentService> services = new ArrayList<>();
    private final List<AgentModule> modules = new ArrayList<>();
    private final OverlayModuleToggleListener moduleToggleListener = new OverlayModuleToggleListener();
    private final OverlayInjectionService overlayInjectionService = new OverlayInjectionService();

    public AgentRegistry() {
        // Register core services and modules here
        services.add(new DebugLoggingService());
        services.add(overlayInjectionService);
        modules.add(new DummyModule());
    }

    public List<AgentService> getServices() {
        return services;
    }

    public List<AgentModule> getModules() {
        return modules;
    }

    public OverlayModuleToggleListener getModuleToggleListener() {
        return moduleToggleListener;
    }

    public OverlayInjectionService getOverlayInjectionService() {
        return overlayInjectionService;
    }
}
