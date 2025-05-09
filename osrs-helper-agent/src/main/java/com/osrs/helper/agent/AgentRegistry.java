package com.osrs.helper.agent;

/**
 * Registry for agent modules and services.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This registry is part of the hybrid patch-based approach.
 */

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
import com.osrs.helper.agent.services.GameStateService;
import com.osrs.helper.agent.services.HookingService;

public class AgentRegistry {
    private final List<AgentService> services = new ArrayList<>();
    private final List<AgentModule> modules = new ArrayList<>();
    private final OverlayModuleToggleListener moduleToggleListener = new OverlayModuleToggleListener();
    private final OverlayInjectionService overlayInjectionService = new OverlayInjectionService();
    private final MenuEntryService menuEntryService = new MenuEntryService();
    private final HookingService hookingService = new HookingService();
    private final GameStateService gameStateService = new GameStateService(hookingService);
    private final AgilityAutomationService agilityAutomationService = new AgilityAutomationService(menuEntryService, gameStateService);

    public AgentRegistry() {
        // Register core services and modules here
        services.add(new DebugLoggingService());
        services.add(overlayInjectionService);
        services.add(agilityAutomationService);
        services.add(menuEntryService);
        services.add(gameStateService);
        services.add(hookingService);
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

    public GameStateService getGameStateService() {
        return gameStateService;
    }

    public HookingService getHookingService() {
        return hookingService;
    }
}
