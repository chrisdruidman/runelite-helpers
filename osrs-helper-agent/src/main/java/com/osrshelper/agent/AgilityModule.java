package com.osrshelper.agent;

import com.osrshelper.agent.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class AgilityModule implements HelperModule, OverlayController.CourseSelectionListener {
    private final Map<String, AgilityCourse> courses = new HashMap<>();
    private final ServiceRegistry serviceRegistry;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile String activeCourseName = null;

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
        if (running.getAndSet(true)) return; // Already running
        if (courses.isEmpty()) {
            System.out.println("No agility courses registered.");
            return;
        }
        // Reset all courses to their starting state before running automation
        for (AgilityCourse c : courses.values()) {
            c.resetCourse();
            // TODO: In the future, improve this to start at any point on the course (not just reset)
        }
        // Use the selected course
        AgilityCourse course = getCourse(activeCourseName);
        if (course == null) {
            // Fallback to first course if none selected
            course = courses.values().iterator().next();
            activeCourseName = course.getName().toLowerCase();
        }
        MouseInputService mouseInputService = serviceRegistry.get(MouseInputService.class);
        if (course == null) {
            System.err.println("[AgilityModule] Course is null!");
            return;
        }
        if (mouseInputService == null) {
            System.err.println("[AgilityModule] MouseInputService is null!");
            return;
        }
        AgilityCourse finalCourse = course;
        new Thread(() -> {
            while (running.get()) {
                try {
                    int actionableId = -1;
                    try {
                        actionableId = finalCourse.getActionableObstacleId();
                    } catch (Exception e) {
                        System.err.println("[AgilityModule] Exception in getActionableObstacleId: " + e);
                        e.printStackTrace();
                    }
                    if (actionableId != -1) {
                        try {
                            mouseInputService.clickGameObject(actionableId);
                        } catch (Exception e) {
                            System.err.println("[AgilityModule] Exception in clickGameObject: " + e);
                            e.printStackTrace();
                        }
                        try {
                            finalCourse.advanceStep();
                        } catch (Exception e) {
                            System.err.println("[AgilityModule] Exception in advanceStep: " + e);
                            e.printStackTrace();
                        }
                        // Randomize wait time between clicks to simulate human behavior
                        int minWait = 1200; // ms (1.2s)
                        int maxWait = 3200; // ms (3.2s)
                        int waitTime = ThreadLocalRandom.current().nextInt(minWait, maxWait + 1);
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ignored) {}
                    } else {
                        // If not actionable, wait a short time before checking again
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                    }
                } catch (Exception e) {
                    System.err.println("[AgilityModule] Exception in automation loop: " + e);
                    e.printStackTrace();
                }
            }
        }, "AgilityModuleThread").start();
    }

    public void registerCourse(String name, AgilityCourse course) {
        courses.put(name.toLowerCase(), course);
    }

    public AgilityCourse getCourse(String name) {
        return courses.get(name.toLowerCase());
    }

    public void runCourse(String name) {
        AgilityCourse course = getCourse(name);
        if (course == null) {
            System.out.println("Course not found: " + name);
        }
        // No run() call needed
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

    public String[] getCourseNames() {
        Set<String> names = courses.keySet();
        return names.toArray(new String[0]);
    }

    @Override
    public void onCourseSelected(String courseName) {
        this.activeCourseName = courseName != null ? courseName.toLowerCase() : null;
        // Optionally reset the selected course when changed
        AgilityCourse course = getCourse(this.activeCourseName);
        if (course != null) {
            course.resetCourse();
        }
    }
}
