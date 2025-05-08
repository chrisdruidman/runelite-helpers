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

    public AgilityAutomationService(MenuEntryService menuEntryService) {
        this.menuEntryService = menuEntryService;
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
        running = true;
        logger.info("Starting agility automation for course: " + course.getName());
        // Example usage of menuEntryService (scaffold):
        // for (AgilityObstacle obstacle : obstacles) {
        //     menuEntryService.interactWithMenuEntry("Jump", obstacle.getObjectId());
        // }
        // TODO: Implement obstacle iteration and RuneLite interaction
    }

    public void stopAutomation() {
        if (running) {
            logger.info("Stopping agility automation.");
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }
}
