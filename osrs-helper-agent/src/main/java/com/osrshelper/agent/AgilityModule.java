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
        MenuActionService menuActionService = serviceRegistry.get(MenuActionService.class);
        if (course == null) {
            System.err.println("[AgilityModule] Course is null!");
            return;
        }
        if (menuActionService == null) {
            System.err.println("[AgilityModule] MenuActionService is null!");
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
                            // Use menu option/target for the actionable obstacle
                            String option = null;
                            String target = null;
                            if (finalCourse instanceof CanifisCourse) {
                                CanifisCourse.Obstacle obs = CanifisCourse.getObstacleById(actionableId);
                                if (obs != null) {
                                    option = obs.option;
                                    target = obs.target;
                                }
                            }
                            // TODO: Add support for other courses here
                            if (option != null && target != null) {
                                boolean success = menuActionService.invokeMenuAction(option, target);
                                if (!success) {
                                    System.err.println("[AgilityModule] Failed to invoke menu action for obstacle id: " + actionableId + " (" + option + ", " + target + ")");
                                }
                            } else {
                                System.err.println("[AgilityModule] No menu option/target mapping for obstacle id: " + actionableId);
                            }
                            // Wait for player animation to change or become idle
                            net.runelite.api.Client client = serviceRegistry.get(net.runelite.api.Client.class);
                            if (client != null && client.getLocalPlayer() != null) {
                                int initialAnimation = client.getLocalPlayer().getAnimation();
                                while (running.get()) {
                                    int currentAnimation = client.getLocalPlayer().getAnimation();
                                    int nextActionableId = finalCourse.getActionableObstacleId();
                                    boolean nextObstacleVisible = false;
                                    if (nextActionableId != -1) {
                                        net.runelite.api.Scene scene = client.getScene();
                                        if (scene != null) {
                                            net.runelite.api.Tile[][][] tiles = scene.getTiles();
                                            outer: for (int z = 0; z < tiles.length; z++) {
                                                for (int x = 0; x < tiles[z].length; x++) {
                                                    for (int y = 0; y < tiles[z][x].length; y++) {
                                                        net.runelite.api.Tile tile = tiles[z][x][y];
                                                        if (tile == null) continue;
                                                        net.runelite.api.GameObject[] gameObjects = tile.getGameObjects();
                                                        if (gameObjects != null) {
                                                            for (net.runelite.api.GameObject obj : gameObjects) {
                                                                if (obj != null && obj.getId() == nextActionableId && (obj.getClickbox() != null || net.runelite.api.Perspective.getCanvasTilePoly(client, obj.getLocalLocation()) != null)) {
                                                                    nextObstacleVisible = true;
                                                                    break outer;
                                                                }
                                                            }
                                                        }
                                                        net.runelite.api.WallObject wall = tile.getWallObject();
                                                        if (wall != null && wall.getId() == nextActionableId && (wall.getClickbox() != null || net.runelite.api.Perspective.getCanvasTilePoly(client, wall.getLocalLocation()) != null)) {
                                                            nextObstacleVisible = true;
                                                            break outer;
                                                        }
                                                        net.runelite.api.DecorativeObject deco = tile.getDecorativeObject();
                                                        if (deco != null && deco.getId() == nextActionableId && (deco.getClickbox() != null || net.runelite.api.Perspective.getCanvasTilePoly(client, deco.getLocalLocation()) != null)) {
                                                            nextObstacleVisible = true;
                                                            break outer;
                                                        }
                                                        net.runelite.api.GroundObject ground = tile.getGroundObject();
                                                        if (ground != null && ground.getId() == nextActionableId && (ground.getClickbox() != null || net.runelite.api.Perspective.getCanvasTilePoly(client, ground.getLocalLocation()) != null)) {
                                                            nextObstacleVisible = true;
                                                            break outer;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // Only break if animation changed (or ended) AND next obstacle is visible
                                    if ((currentAnimation == -1 || currentAnimation != initialAnimation) && nextObstacleVisible) {
                                        break; // Obstacle cleared and next is visible
                                    }
                                    // No fallback timeout: only proceed when the above is true
                                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                                }
                            }
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

    public void stop() {
        running.set(false);
    }
}
