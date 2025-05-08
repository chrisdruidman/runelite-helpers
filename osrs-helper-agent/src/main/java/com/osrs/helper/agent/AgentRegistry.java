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
import com.osrs.helper.agent.helpermodules.agility.AgilityModule;
import com.osrs.helper.agent.services.AgilityAutomationService;
import com.osrs.helper.agent.services.MenuEntryService;

public class AgentRegistry {
    private final List<AgentService> services = new ArrayList<>();
    private final List<AgentModule> modules = new ArrayList<>();
    private final OverlayModuleToggleListener moduleToggleListener = new OverlayModuleToggleListener();
    private final OverlayInjectionService overlayInjectionService = new OverlayInjectionService();
    private final MenuEntryService menuEntryService = new MenuEntryService();
    private final AgilityAutomationService agilityAutomationService = new AgilityAutomationService(menuEntryService);

    public AgentRegistry() {
        // Register core services and modules here
        services.add(new DebugLoggingService());
        services.add(overlayInjectionService);
        services.add(agilityAutomationService);
        services.add(menuEntryService);
        modules.add(new DummyModule());
        modules.add(new AgilityModule(agilityAutomationService));
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

    public AgilityAutomationService getAgilityAutomationService() {
        return agilityAutomationService;
    }

    public MenuEntryService getMenuEntryService() {
        return menuEntryService;
    }
}
