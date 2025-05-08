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
    private static final int MAX_RETRIES = 3;
    private boolean running = false;
    private final MenuEntryService menuEntryService;
    private final GameStateService gameStateService;
    private AgilityCourse currentCourse;
    private List<AgilityObstacle> currentObstacles;
    private int currentIndex;
    private int lapCount = 0;
    private int maxLaps = 1; // Set to -1 for infinite laps
    private enum State { IDLE, RUNNING, WAITING, ERROR, PAUSED }
    private State state = State.IDLE;
    private int currentRetry = 0;

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
        this.lapCount = 0;
        this.state = State.RUNNING;
        this.currentRetry = 0;
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
            state = State.IDLE;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void stepToNextObstacle() {
        if (!running || currentObstacles == null) {
            logger.info("Agility automation stopped.");
            state = State.IDLE;
            stopAutomation();
            return;
        }
        if (currentIndex >= currentObstacles.size()) {
            lapCount++;
            logger.info("Completed lap " + lapCount + ".");
            if (maxLaps > 0 && lapCount >= maxLaps) {
                logger.info("Max laps reached. Stopping automation.");
                state = State.IDLE;
                stopAutomation();
                return;
            }
            currentIndex = 0;
            logger.info("Starting next lap.");
        }
        state = State.RUNNING;
        currentRetry = 0;
        AgilityObstacle obstacle = currentObstacles.get(currentIndex);
        handleObstacle(obstacle);
    }

    private void handleObstacle(AgilityObstacle obstacle) {
        logger.info("Handling obstacle: " + obstacle.getName() + " (" + obstacle.getObjectId() + ")");
        state = State.WAITING;
        // Wait for player to be at/near the obstacle
        if (!waitForPlayerAtPosition(obstacle.getObstaclePosition(), 3, 3000)) {
            handleRetryOrError("Player not at required position for obstacle: " + obstacle.getName(), obstacle);
            return;
        }
        boolean success = menuEntryService.interactWithMenuEntry(obstacle.getMenuAction(), obstacle.getObjectId());
        if (success) {
            logger.info("Successfully interacted with obstacle: " + obstacle.getName());
            if (!waitForPlayerToAnimate(3000)) {
                handleRetryOrError("Player did not start animating for obstacle: " + obstacle.getName(), obstacle);
                return;
            }
            if (!waitForPlayerToStopAnimating(7000)) {
                handleRetryOrError("Player did not finish obstacle: " + obstacle.getName(), obstacle);
                return;
            }
            // REQUIRED: Validate player is at expected position and/or animation
            if (!validatePlayerAfterObstacle(obstacle)) {
                handleRetryOrError("Player did not reach expected state after obstacle: " + obstacle.getName(), obstacle);
                return;
            }
            currentIndex++;
            stepToNextObstacle();
        } else {
            handleRetryOrError("Failed to interact with obstacle: " + obstacle.getName(), obstacle);
        }
    }

    private void handleRetryOrError(String message, AgilityObstacle obstacle) {
        logger.warning(message + " (retry " + (currentRetry + 1) + "/" + MAX_RETRIES + ")");
        currentRetry++;
        if (currentRetry < MAX_RETRIES) {
            logger.info("Retrying obstacle: " + obstacle.getName());
            handleObstacle(obstacle);
        } else {
            logger.severe("Max retries reached for obstacle: " + obstacle.getName() + ". Aborting automation.");
            state = State.ERROR;
            stopAutomation();
        }
    }

    private boolean waitForPlayerAtPosition(Object requiredPosition, int tolerance, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (!running) return false;
            Object pos = gameStateService.getPlayerPosition();
            if (pos instanceof com.osrs.helper.agent.helpermodules.agility.WorldPosition) {
                com.osrs.helper.agent.helpermodules.agility.WorldPosition wp = (com.osrs.helper.agent.helpermodules.agility.WorldPosition) pos;
                com.osrs.helper.agent.helpermodules.agility.WorldPosition req = (com.osrs.helper.agent.helpermodules.agility.WorldPosition) requiredPosition;
                if (Math.abs(wp.x - req.x) <= tolerance && Math.abs(wp.y - req.y) <= tolerance && wp.plane == req.plane) {
                    return true;
                }
            }
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private boolean validatePlayerAfterObstacle(AgilityObstacle obstacle) {
        Object pos = gameStateService.getPlayerPosition();
        boolean atExpectedPos = false;
        if (pos instanceof com.osrs.helper.agent.helpermodules.agility.WorldPosition) {
            com.osrs.helper.agent.helpermodules.agility.WorldPosition wp = (com.osrs.helper.agent.helpermodules.agility.WorldPosition) pos;
            com.osrs.helper.agent.helpermodules.agility.WorldPosition expected = obstacle.getExpectedPlayerPosition();
            atExpectedPos = (wp.x == expected.x && wp.y == expected.y && wp.plane == expected.plane);
        }
        boolean correctAnim = (obstacle.getExpectedAnimationId() == -1) || (gameStateService.isPlayerAnimating() && obstacle.getExpectedAnimationId() == getCurrentAnimationId());
        return atExpectedPos && correctAnim;
    }

    private int getCurrentAnimationId() {
        // Use GameStateService/HookingService to get the current animation ID
        return gameStateService.getCurrentPlayerAnimationId();
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

    public void setMaxLaps(int maxLaps) {
        this.maxLaps = maxLaps;
    }

    public int getLapCount() {
        return lapCount;
    }

    public State getState() {
        return state;
    }

    private void handleError(String message) {
        logger.warning(message);
        // TODO: Add retry logic, user feedback, or abort as needed
        stopAutomation();
    }
}
