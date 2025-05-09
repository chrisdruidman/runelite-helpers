package com.osrs.helper.agent.services;

/**
 * Base interface for all agent services.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. All services must comply with the hybrid patch-based approach.
 * Services should be stateless or manage their own state.
 */
public interface AgentService {
    /**
     * Called when the service is initialized by the registry.
     */
    void initialize();

    /**
     * Called when the service is being shut down.
     */
    void shutdown();
}
