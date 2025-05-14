package com.osrs.helper.agent.helpermodules.agility;

import com.osrs.helper.agent.helpermodules.AgentModule;
import com.osrs.helper.agent.services.AgilityAutomationService;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Agility helper module for course automation.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This module is part of the hybrid patch-based approach.
 *
 * Minimal scaffold for the Agility helper module.
 * This module will be extended to provide automation and overlays for the Agility skill.
 */
public class AgilityModule implements AgentModule {
    private boolean enabled = false;
    private final Map<String, AgilityCourse> courses = new LinkedHashMap<>();
    private AgilityCourse selectedCourse = null;
    private final AgilityAutomationService automationService;

    public AgilityModule(AgilityAutomationService automationService) {
        this.automationService = automationService;
        // Register available courses
        AgilityCourse canifis = new CanifisCourse();
        courses.put(canifis.getName(), canifis);
        // Future: add more courses here
    }

    @Override
    public void onEnable() {
        if (selectedCourse == null) {
            System.err.println("[AgilityModule] Cannot enable: No course selected!");
            enabled = false;
            return;
        }
        enabled = true;
        System.out.println("AgilityModule enabled for course: " + selectedCourse.getName());
        // Start automation
        automationService.startAutomation(selectedCourse, getCurrentObstacles());
    }

    @Override
    public void onDisable() {
        enabled = false;
        System.out.println("AgilityModule disabled");
        // Stop automation
        automationService.stopAutomation();
    }

    @Override
    public String getName() {
        return "Agility Helper";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public java.util.Set<String> getCourseNames() {
        return courses.keySet();
    }

    public void setSelectedCourse(String courseName) {
        this.selectedCourse = courses.get(courseName);
    }

    public AgilityCourse getSelectedCourse() {
        return selectedCourse;
    }

    /**
     * Returns the list of obstacles for the currently selected course, or null if none selected.
     */
    public java.util.List<AgilityObstacle> getCurrentObstacles() {
        return selectedCourse != null ? ((selectedCourse instanceof CanifisCourse) ? ((CanifisCourse) selectedCourse).getObstacles() : null) : null;
    }

    public void setMaxLaps(int maxLaps) {
        if (automationService != null) {
            automationService.setMaxLaps(maxLaps);
        }
    }

    public void startAutomation() {
        if (selectedCourse != null && automationService != null) {
            automationService.startAutomation(selectedCourse, getCurrentObstacles());
        }
    }

    public void stopAutomation() {
        if (automationService != null) {
            automationService.stopAutomation();
        }
    }
}
