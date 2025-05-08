package com.osrs.helper.agent;

import com.osrs.helper.agent.services.*;
import com.osrs.helper.agent.helpermodules.*;
import java.util.ArrayList;
import java.util.List;

public class AgentRegistry {
    private final List<Object> services = new ArrayList<>();
    private final List<Object> modules = new ArrayList<>();

    public AgentRegistry() {
        // Register core services and modules here
        // Example: services.add(new MouseInputService());
        // Example: modules.add(new AgilityModule());
    }

    public List<Object> getServices() {
        return services;
    }

    public List<Object> getModules() {
        return modules;
    }
}
