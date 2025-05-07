package com.osrshelper.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.*;

public class Main {
    private static final String LOG_FILE = "osrs-helper-launcher/launcher-output";
    private static Logger logger;

    public static void main(String[] args) {
        setupLogging();
        logger.info("OSRS Helper Launcher started.");
        // ...future extensibility here...
    }

    private static void setupLogging() {
        logger = Logger.getLogger("OSRSHelperLauncher");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        // Clear log file at start
        try {
            Files.write(Path.of(LOG_FILE), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }

        // Console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        // File handler
        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Failed to set up file logging: " + e.getMessage());
        }
    }
}
