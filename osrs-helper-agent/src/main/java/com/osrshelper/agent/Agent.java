package com.osrshelper.agent;

// ...existing imports...
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Agent {
    private static final String LOG_FILE = "agent-output";
    private static final Logger logger = Logger.getLogger("AgentLogger");

    public static void premain(String agentArgs, Instrumentation inst) {
        setupLogging();
        logger.info("Agent started at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        // ...future extensible agent logic...
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
}
