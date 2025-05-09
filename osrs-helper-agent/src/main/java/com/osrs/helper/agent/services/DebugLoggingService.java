package com.osrs.helper.agent.services;

import java.util.logging.Logger;

/**
 * Service for debug and error logging for the agent.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This service is part of the hybrid patch-based approach.
 */
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
