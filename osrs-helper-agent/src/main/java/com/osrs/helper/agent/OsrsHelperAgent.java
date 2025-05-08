package com.osrs.helper.agent;

import java.lang.instrument.Instrumentation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.*;

public class OsrsHelperAgent {
    private static final Logger logger = Logger.getLogger("OsrsHelperAgent");
    private static FileHandler fileHandler;

    public static void premain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("OsrsHelperAgent started with premain");
        // TODO: Initialize registry, services, modules, and overlay
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("OsrsHelperAgent started with agentmain");
        // TODO: Initialize registry, services, modules, and overlay
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
}
