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
                            CanifisCourse.Obstacle obs = null;
                            
                            if (finalCourse instanceof CanifisCourse) {
                                obs = CanifisCourse.getObstacleById(actionableId);
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
                                    // If we failed to find the menu entry, wait before trying again
                                    // This prevents the module from immediately moving to the next obstacle
                                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                                    continue; // Skip to next loop iteration without advancing step
                                }
                                
                                // We successfully clicked an obstacle, now wait for the animation/movement to complete
                                net.runelite.api.Client client = serviceRegistry.get(net.runelite.api.Client.class);
                                if (client != null && client.getLocalPlayer() != null) {
                                    int initialAnimation = client.getLocalPlayer().getAnimation();
                                    int initialX = client.getLocalPlayer().getLocalLocation().getX();
                                    int initialY = client.getLocalPlayer().getLocalLocation().getY();
                                    int initialZ = client.getLocalPlayer().getLocalLocation().getSceneY();
                                    
                                    // Track when animation starts (may not start immediately after click)
                                    long startTime = System.currentTimeMillis();
                                    boolean animationStarted = false;
                                    boolean obstacleCompleted = false;
                                    
                                    // Wait loop for obstacle completion
                                    while (running.get() && !obstacleCompleted) {
                                        if (client.getLocalPlayer() == null) {
                                            break; // Player no longer available
                                        }
                                        
                                        int currentAnimation = client.getLocalPlayer().getAnimation();
                                        int currentX = client.getLocalPlayer().getLocalLocation().getX();
                                        int currentY = client.getLocalPlayer().getLocalLocation().getY();
                                        int currentZ = client.getLocalPlayer().getLocalLocation().getSceneY();
                                        
                                        // Check if animation started
                                        if (!animationStarted && currentAnimation != -1 && currentAnimation != initialAnimation) {
                                            animationStarted = true;
                                            System.out.println("[AgilityModule] Animation started for obstacle: " + actionableId);
                                        }
                                        
                                        // Criteria for obstacle completion: 
                                        // 1. Animation changed and then went back to idle
                                        // 2. Player position changed significantly (moved to next obstacle)
                                        // 3. Or a significant time passed (fallback)
                                        
                                        boolean positionChanged = Math.abs(currentX - initialX) > 300 || 
                                                                 Math.abs(currentY - initialY) > 300 ||
                                                                 currentZ != initialZ;
                                        
                                        boolean animationComplete = animationStarted && currentAnimation == -1;
                                        long timeElapsed = System.currentTimeMillis() - startTime;
                                        
                                        if ((animationComplete && positionChanged) || 
                                            (animationStarted && timeElapsed > 5000) ||  // 5 second timeout after animation starts
                                            timeElapsed > 10000) {                       // 10 second absolute timeout
                                            obstacleCompleted = true;
                                            System.out.println("[AgilityModule] Obstacle completed: " + actionableId);
                                        }
                                        
                                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                                    }
                                }
                            } else {
                                System.err.println("[AgilityModule] No menu option/target mapping for obstacle id: " + actionableId);
                            }
                        } catch (Exception e) {
                            System.err.println("[AgilityModule] Exception in processing obstacle: " + e);
                            e.printStackTrace();
                        }
                        
                        // Only advance step after we've confirmed obstacle completion or encountered an error
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

    /**
     * Get the currently active agility course
     * @return The active AgilityCourse or null if none selected
     */
    public AgilityCourse getActiveCourse() {
        return getCourse(activeCourseName);
    }

    public void stop() {
        running.set(false);
    }
}
