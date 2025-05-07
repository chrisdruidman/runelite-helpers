package com.osrshelper.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple registry for shared service instances.
 */
public class ServiceRegistry {
    private final Map<Class<?>, Object> services = new HashMap<>();

    /**
     * Register a service instance by its class type.
     */
    public <T> void register(Class<T> clazz, T instance) {
        services.put(clazz, instance);
    }

    /**
     * Retrieve a service instance by its class type.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) services.get(clazz);
    }
}
