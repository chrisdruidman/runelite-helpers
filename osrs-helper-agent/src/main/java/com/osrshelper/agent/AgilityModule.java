package com.osrshelper.agent;

import com.osrshelper.agent.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

public class AgilityModule implements Module {
    private final Map<String, AgilityCourse> courses = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    public AgilityModule(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        // Register courses here
        registerCourse("canifis", new CanifisCourse(serviceRegistry.get(GameStateProvider.class)));
        // Add more courses as needed
    }

    @Override
    public String getName() {
        return "agility";
    }

    @Override
    public void run() {
        GameStateProvider gameStateProvider = serviceRegistry.get(GameStateProvider.class);
        MouseInputService mouseInputService = serviceRegistry.get(MouseInputService.class);
        // Default: run the first registered course (can be improved for user selection)
        if (!courses.isEmpty()) {
            courses.values().iterator().next().run();
        } else {
            System.out.println("No agility courses registered.");
        }
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

    /**
     * Allows a course to request a mouse click at the given coordinates.
     * This keeps input logic at the module level.
     */
    @Override
    public void clickAt(int x, int y) {
        MouseInputService mouseInputService = serviceRegistry.get(MouseInputService.class);
        mouseInputService.clickAt(x, y);
    }

    /**
     * Allows a course to request a mouse click on a game object by ID.
     * This keeps input logic at the module level.
     */
    @Override
    public void clickGameObject(int id) {
        MouseInputService mouseInputService = serviceRegistry.get(MouseInputService.class);
        mouseInputService.clickGameObject(id);
    }
}
