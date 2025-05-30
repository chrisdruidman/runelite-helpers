package com.osrs.helper.agent;

/**
 * Main entry point for the OSRS Helper Agent.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This agent is part of the hybrid patch-based approach.
 */

import java.lang.instrument.Instrumentation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.*;
import com.osrs.helper.agent.overlay.OverlayWindow;
import com.osrs.helper.agent.listeners.OverlayModuleToggleListener;
import com.osrs.helper.agent.services.AgentService;

public class OsrsHelperAgent {
    private static final Logger logger = Logger.getLogger("OsrsHelperAgent");
    private static FileHandler fileHandler;
    private static OverlayWindow overlayWindow;

    public static void premain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("OsrsHelperAgent started with premain");
        // Only overlay injection uses ASM/injected hooks. No transformers registered here.
        initializeAgent();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("OsrsHelperAgent started with agentmain");
        // Only overlay injection uses ASM/injected hooks. No transformers registered here.
        initializeAgent();
    }

    private static void setupLogging() {
        try {
            File logFile = new File("agent-output");
            if (logFile.exists()) {
                new FileWriter(logFile, false).close(); // Clear file
            }
            fileHandler = new FileHandler("agent-output", false);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(true);
        } catch (IOException e) {
            System.err.println("Failed to set up logging: " + e.getMessage());
        }
    }

    private static void initializeAgent() {
        AgentRegistry registry = new AgentRegistry();
        // Initialize all services
        for (AgentService service : registry.getServices()) {
            service.initialize();
        }
        // Initialize and show the overlay with module controls
        overlayWindow = new OverlayWindow(registry.getModules(), registry.getModuleToggleListener());
        java.awt.EventQueue.invokeLater(() -> overlayWindow.showOverlay());
        logger.info("All services initialized. Modules are disabled by default. Overlay started.");
    }

    public static void launchOverlay() {
        logger.info("launchOverlay() called via ASM-injected bytecode");
        java.awt.EventQueue.invokeLater(() -> {
            if (overlayWindow != null) {
                overlayWindow.showOverlay();
            }
        });
    }
}
