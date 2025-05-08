package com.osrs.helper.agent.services;

import java.util.logging.Logger;

public class DebugLoggingService implements AgentService {
    private static final Logger logger = Logger.getLogger("DebugLoggingService");
    private boolean initialized = false;

    @Override
    public void initialize() {
        initialized = true;
        logger.info("DebugLoggingService initialized");
    }

    @Override
    public void shutdown() {
        logger.info("DebugLoggingService shutdown");
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
