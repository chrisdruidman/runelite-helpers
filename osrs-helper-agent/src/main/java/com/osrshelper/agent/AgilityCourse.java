package com.osrshelper.agent;

public interface AgilityCourse {
    /**
     * Run the course logic (e.g., obstacle navigation, state handling).
     */
    void run();

    /**
     * @return The name of the course (e.g., "Canifis Rooftop Course").
     */
    String getName();
}
