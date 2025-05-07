package com.osrshelper.agent;

import java.util.HashMap;
import java.util.Map;

public class AgilityModule {
    private final Map<String, AgilityCourse> courses = new HashMap<>();

    public AgilityModule() {
        // Register courses here
        registerCourse("canifis", new CanifisCourse());
        // Add more courses as needed
    }

    public void registerCourse(String name, AgilityCourse course) {
        courses.put(name.toLowerCase(), course);
    }

    public AgilityCourse getCourse(String name) {
        return courses.get(name.toLowerCase());
    }

    public void runCourse(String name) {
        AgilityCourse course = getCourse(name);
        if (course != null) {
            course.run();
        } else {
            System.out.println("Course not found: " + name);
        }
    }
}
