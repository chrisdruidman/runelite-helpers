package com.osrs.helper.agent;

import com.osrs.helper.agent.services.*;
import com.osrs.helper.agent.helpermodules.*;
import java.util.ArrayList;
import java.util.List;
import com.osrs.helper.agent.services.AgentService;
import com.osrs.helper.agent.helpermodules.AgentModule;
import com.osrs.helper.agent.services.DebugLoggingService;
import com.osrs.helper.agent.helpermodules.dummy.DummyModule;

public class AgentRegistry {
    private final List<AgentService> services = new ArrayList<>();
    private final List<AgentModule> modules = new ArrayList<>();

    public AgentRegistry() {
        // Register core services and modules here
        services.add(new DebugLoggingService());
        modules.add(new DummyModule());
    }

    public List<AgentService> getServices() {
        return services;
    }

    public List<AgentModule> getModules() {
        return modules;
    }
}
