package com.osrs.helper.agent.services;

import com.osrs.helper.agent.helpermodules.agility.AgilityCourse;
import com.osrs.helper.agent.helpermodules.agility.AgilityObstacle;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service responsible for automating the selected agility course.
 * This is a scaffold for future extensibility and modularity.
 */
public class AgilityAutomationService implements AgentService {
    private static final Logger logger = Logger.getLogger("AgilityAutomationService");
    private boolean running = false;
    private final MenuEntryService menuEntryService;
    private final GameStateService gameStateService;
    private AgilityCourse currentCourse;
    private List<AgilityObstacle> currentObstacles;
    private int currentIndex;

    public AgilityAutomationService(MenuEntryService menuEntryService, GameStateService gameStateService) {
        this.menuEntryService = menuEntryService;
        this.gameStateService = gameStateService;
    }

    @Override
    public void initialize() {
        logger.info("AgilityAutomationService initialized");
    }

    @Override
    public void shutdown() {
        logger.info("AgilityAutomationService shutdown");
        stopAutomation();
    }

    public void startAutomation(AgilityCourse course, List<AgilityObstacle> obstacles) {
        if (course == null || obstacles == null || obstacles.isEmpty()) {
            logger.warning("Cannot start agility automation: No course or obstacles provided.");
            return;
        }
        this.currentCourse = course;
        this.currentObstacles = obstacles;
        this.currentIndex = 0;
        this.running = true;
        logger.info("Starting agility automation for course: " + course.getName());
        stepToNextObstacle();
    }

    public void stopAutomation() {
        if (running) {
            logger.info("Stopping agility automation.");
            running = false;
            currentCourse = null;
            currentObstacles = null;
            currentIndex = 0;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void stepToNextObstacle() {
        if (!running || currentObstacles == null || currentIndex >= currentObstacles.size()) {
            logger.info("Agility course complete or stopped.");
            stopAutomation();
            return;
        }
        AgilityObstacle obstacle = currentObstacles.get(currentIndex);
        handleObstacle(obstacle);
    }

    private void handleObstacle(AgilityObstacle obstacle) {
        logger.info("Handling obstacle: " + obstacle.getName() + " (" + obstacle.getObjectId() + ")");
        boolean success = menuEntryService.interactWithMenuEntry("Jump", obstacle.getObjectId());
        if (success) {
            logger.info("Successfully interacted with obstacle: " + obstacle.getName());
            if (!waitForPlayerToAnimate(3000)) {
                handleError("Player did not start animating for obstacle: " + obstacle.getName());
                return;
            }
            if (!waitForPlayerToStopAnimating(7000)) {
                handleError("Player did not finish obstacle: " + obstacle.getName());
                return;
            }
            currentIndex++;
            stepToNextObstacle();
        } else {
            handleError("Failed to interact with obstacle: " + obstacle.getName());
            // Do NOT step to next obstacle on failure
        }
    }

    /**
     * Waits for the player to start animating, up to the given timeout (ms).
     */
    private boolean waitForPlayerToAnimate(long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (!running) return false;
            // Use real-time game state from GameStateService
            if (gameStateService.isPlayerAnimating()) return true;
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    /**
     * Waits for the player to stop animating, up to the given timeout (ms).
     */
    private boolean waitForPlayerToStopAnimating(long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (!running) return false;
            if (!gameStateService.isPlayerAnimating()) return true;
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private void handleError(String message) {
        logger.warning(message);
        // TODO: Add retry logic, user feedback, or abort as needed
        stopAutomation();
    }
}
