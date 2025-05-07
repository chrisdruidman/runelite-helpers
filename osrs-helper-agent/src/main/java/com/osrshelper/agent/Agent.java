package com.osrshelper.agent;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import net.runelite.api.Client;
import net.runelite.client.input.MouseManager;

public class Agent {
    private static final String LOG_FILE = "agent-output";
    private static final Logger logger = Logger.getLogger("AgentLogger");
    private static final Map<String, Module> modules = new HashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("Agent started at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        try {
            // Use reflection to find the RuneLite Client instance
            Class<?> clientClass = Class.forName("net.runelite.client.RuneLite");
            Object runeliteInstance = null;
            for (Object o : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                // This is a placeholder for more robust client instance discovery if needed
            }
            // Try to find a static field of type Client
            Client client = null;
            for (Class<?> clazz : clientClass.getDeclaredClasses()) {
                if (Client.class.isAssignableFrom(clazz)) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (Client.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            client = (Client) field.get(null);
                            break;
                        }
                    }
                }
            }
            if (client == null) {
                logger.severe("Could not find RuneLite Client instance via reflection.");
                return;
            }
            // Find MouseManager instance via reflection
            Class<?> mouseManagerClass = Class.forName("net.runelite.client.input.MouseManager");
            Object mouseManagerInstance = null;
            // Try to find a static or singleton instance of MouseManager
            // Look for a static field or singleton pattern
            for (Field field : mouseManagerClass.getDeclaredFields()) {
                if (MouseManager.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    mouseManagerInstance = field.get(null);
                    break;
                }
            }
            if (mouseManagerInstance == null) {
                // Try to find MouseManager from the RuneLite client instance if available
                for (Field field : clientClass.getDeclaredFields()) {
                    if (MouseManager.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        mouseManagerInstance = field.get(runeliteInstance);
                        break;
                    }
                }
            }
            if (mouseManagerInstance == null) {
                logger.severe("Could not find MouseManager instance via reflection.");
                return;
            }
            // Instantiate the real game state provider
            RealGameStateProvider gameStateProvider = new RealGameStateProvider(client);
            // Create ServiceRegistry and register services
            ServiceRegistry serviceRegistry = new ServiceRegistry();
            serviceRegistry.register(Client.class, client);
            serviceRegistry.register(RealGameStateProvider.class, gameStateProvider);
            serviceRegistry.register(MouseManager.class, (MouseManager) mouseManagerInstance);
            // Create MouseInputService with all dependencies
            MouseInputService mouseInputService = new MouseInputService((MouseManager) mouseManagerInstance, gameStateProvider, client);
            serviceRegistry.register(MouseInputService.class, mouseInputService);
            // Register modules, passing services as needed
            registerModule(new AgilityModule(gameStateProvider, mouseInputService));
            // Launch overlay for toggling modules
            OverlayController overlay = new OverlayController(modules);
            overlay.show();
            // Example: run the Agility module if enabled
            if (overlay.isModuleEnabled("agility")) {
                runModule("agility");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Agent initialization failed", e);
        }
    }

    private static void setupLogging() {
        try {
            // Clear the log file at the start of each run
            File logFile = new File(LOG_FILE);
            if (logFile.exists()) {
                new PrintWriter(logFile).close();
            }
            // Set up file handler
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            // Set up console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            // Configure logger
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to set up logging: " + e.getMessage());
        }
    }

    public static void registerModule(Module module) {
        modules.put(module.getName().toLowerCase(), module);
        logger.info("Registered module: " + module.getName());
    }

    public static Module getModule(String name) {
        return modules.get(name.toLowerCase());
    }

    public static void runModule(String name) {
        Module module = getModule(name);
        if (module != null) {
            logger.info("Running module: " + name);
            module.run();
        } else {
            logger.warning("Module not found: " + name);
        }
    }
}
