package com.osrs.helper.agent.services;

/**
 * Base interface for all agent services (e.g., mouse input, menu entry).
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
