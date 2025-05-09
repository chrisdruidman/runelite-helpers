package com.osrs.helper.agent.helpermodules.agility;

/**
 * Represents an agility course for automation.
 * <b>IMPORTANT:</b> Only the overlay uses injected hooks/ASM. All other interaction with RuneLite must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This class is part of the hybrid patch-based approach.
 */
public interface AgilityCourse {
    /**
     * @return the display name of the course
     */
    String getName();
    // Future extensibility: add methods for course-specific logic
}
