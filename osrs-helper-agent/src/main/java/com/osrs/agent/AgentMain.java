package com.osrs.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    // Toggle for enabling/disabling automation
    private static volatile boolean automationEnabled = false;

    public static void setAutomationEnabled(boolean enabled) {
        automationEnabled = enabled;
        System.out.println("[OSRS Helper Agent] Automation " + (enabled ? "enabled" : "disabled"));
    }

    public static boolean isAutomationEnabled() {
        return automationEnabled;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[OSRS Helper Agent] Agent started. Initializing ByteBuddy...");
        // Print process ID and JVM arguments
        String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        System.out.println("[OSRS Helper Agent] Process ID: " + pid);
        System.out.println("[OSRS Helper Agent] JVM Args: " + java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments());
        // Print retransformation support
        System.out.println("[OSRS Helper Agent] isRetransformClassesSupported: " + inst.isRetransformClassesSupported());
        // Print total loaded classes and a sample
        Class<?>[] loaded = inst.getAllLoadedClasses();
        System.out.println("[OSRS Helper Agent] Loaded class count: " + loaded.length);
        for (int i = 0; i < Math.min(20, loaded.length); i++) {
            System.out.println("[OSRS Helper Agent] Loaded: " + loaded[i].getName());
        }
        // Log all loaded classes containing 'runelite' to check class loading order
        for (Class<?> clazz : loaded) {
            if (clazz.getName().toLowerCase().contains("runelite")) {
                System.out.println("[OSRS Helper Agent] Already loaded: " + clazz.getName());
            }
        }
        // Install generic reusable hook with detailed ByteBuddy logging and retransformation
        new AgentBuilder.Default()
            .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
            .with(AgentBuilder.InstallationListener.StreamWriting.toSystemOut())
            .with(RedefinitionStrategy.RETRANSFORMATION)
            .type(ElementMatchers.nameContainsIgnoreCase("runelite"))
            .transform(new GenericHook(ElementMatchers.any()))
            .type(ElementMatchers.named("net.runelite.client.input.MouseManager"))
            .transform(new MouseAutomationHook())
            .type(ElementMatchers.named("net.runelite.client.callback.Hooks"))
            .transform(new GameTickHook())
            // Hook RuneLite.main to register overlay after client startup
            .type(ElementMatchers.named("net.runelite.client.RuneLite"))
            .transform((builder, typeDescription, classLoader, module, pd) ->
                builder.visit(Advice.to(RuneLiteMainAdvice.class).on(ElementMatchers.named("main")))
            )
            .installOn(inst);
    }

    // Advice to run after RuneLite.main executes
    public static class RuneLiteMainAdvice {
        @Advice.OnMethodExit
        static void onExit() {
            System.out.println("[OSRS Helper Agent] RuneLite.main finished, registering overlay...");
            registerOverlay();
        }
    }

    public static void registerOverlay() {
        try {
            System.out.println("[OSRS Helper Agent] Attempting to register overlay...");
            // Get the RuneLite injector
            Class<?> runeLiteClass = Class.forName("net.runelite.client.RuneLite");
            System.out.println("[OSRS Helper Agent] Loaded RuneLite class: " + runeLiteClass);
            java.lang.reflect.Field injectorField = runeLiteClass.getDeclaredField("injector");
            injectorField.setAccessible(true);
            Object injector = injectorField.get(null);
            System.out.println("[OSRS Helper Agent] Injector: " + injector);
            if (injector == null) {
                System.out.println("[OSRS Helper Agent] Injector is null, aborting overlay registration.");
                return;
            }
            // Get OverlayManager instance
            Object overlayManager = injector.getClass().getMethod("getInstance", Class.class)
                .invoke(injector, Class.forName("net.runelite.client.ui.overlay.OverlayManager"));
            System.out.println("[OSRS Helper Agent] OverlayManager: " + overlayManager);
            if (overlayManager == null) {
                System.out.println("[OSRS Helper Agent] OverlayManager is null, aborting overlay registration.");
                return;
            }
            // Create AutomationOverlay instance
            Class<?> overlayClass = Class.forName("com.osrs.agent.AutomationOverlay");
            System.out.println("[OSRS Helper Agent] Loaded AutomationOverlay class: " + overlayClass);
            Object overlay = overlayClass.getConstructor().newInstance();
            System.out.println("[OSRS Helper Agent] Created overlay instance: " + overlay);
            // Inject dependencies if needed
            injector.getClass().getMethod("injectMembers", Object.class).invoke(injector, overlay);
            System.out.println("[OSRS Helper Agent] Dependencies injected into overlay.");
            // Register overlay
            overlayManager.getClass().getMethod("add", Class.forName("net.runelite.client.ui.overlay.Overlay")).invoke(overlayManager, overlay);
            System.out.println("[OSRS Helper Agent] AutomationOverlay registered successfully.");
        } catch (Exception e) {
            System.out.println("[OSRS Helper Agent] Exception during overlay registration:");
            e.printStackTrace();
        }
    }

    /**
     * Generic reusable transformer for method entry/exit hooks.
     */
    public static class GenericHook implements AgentBuilder.Transformer {
        private final ElementMatcher<? super MethodDescription> methodMatcher;

        public GenericHook(ElementMatcher<? super MethodDescription> methodMatcher) {
            this.methodMatcher = methodMatcher;
        }

        @Override
        public net.bytebuddy.dynamic.DynamicType.Builder<?> transform(
                net.bytebuddy.dynamic.DynamicType.Builder<?> builder,
                net.bytebuddy.description.type.TypeDescription typeDescription,
                ClassLoader classLoader,
                net.bytebuddy.utility.JavaModule module,
                java.security.ProtectionDomain protectionDomain) {
            return builder.visit(Advice.to(GenericAdvice.class).on(methodMatcher));
        }
    }

    /**
     * Example advice for generic entry/exit logging or automation.
     */
    public static class GenericAdvice {
        @Advice.OnMethodEnter
        static void onEnter(@Advice.Origin String method) {
            // Placeholder for generic entry logic
        }
        @Advice.OnMethodExit
        static void onExit(@Advice.Origin String method) {
            // Placeholder for generic exit logic
        }
    }

    /**
     * Transformer for mouse automation hooks.
     */
    public static class MouseAutomationHook implements AgentBuilder.Transformer {
        @Override
        public net.bytebuddy.dynamic.DynamicType.Builder<?> transform(
                net.bytebuddy.dynamic.DynamicType.Builder<?> builder,
                net.bytebuddy.description.type.TypeDescription typeDescription,
                ClassLoader classLoader,
                net.bytebuddy.utility.JavaModule module,
                java.security.ProtectionDomain protectionDomain) {
            // Target mouse event processing methods
            return builder
                .visit(Advice.to(MouseAutomationAdvice.class).on(
                    ElementMatchers.named("processMousePressed")
                        .or(ElementMatchers.named("processMouseReleased"))
                        .or(ElementMatchers.named("processMouseClicked"))
                        .or(ElementMatchers.named("processMouseMoved"))
                        .or(ElementMatchers.named("processMouseDragged"))
                ));
        }
    }

    /**
     * Advice for simulating human-like mouse actions.
     * Extend this to inject custom mouse movement/automation logic.
     */
    public static class MouseAutomationAdvice {
        private static volatile Integer targetX = null;
        private static volatile Integer targetY = null;

        public static void setTarget(int x, int y) {
            targetX = x;
            targetY = y;
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.This(optional = true) Object self, @Advice.Origin String method, @Advice.AllArguments Object[] args) {
            if (!AgentMain.isAutomationEnabled()) return false;
            try {
                Class<?> mouseEventClass = Class.forName("java.awt.event.MouseEvent");
                java.awt.Component component = null;
                if (args.length > 0 && args[0] instanceof java.awt.event.MouseEvent) {
                    component = ((java.awt.event.MouseEvent) args[0]).getComponent();
                }
                if (component == null) return false;
                java.awt.Point start = new java.awt.Point(100, 100); // TODO: get real current mouse position
                java.awt.Point end;
                if (targetX != null && targetY != null) {
                    // Use the automation target if set
                    end = new java.awt.Point(targetX, targetY);
                    // Reset after use
                    targetX = null;
                    targetY = null;
                } else {
                    // Fallback: default target
                    end = new java.awt.Point(200, 200);
                }
                // Add randomisation to the end point target for more authentic movement
                java.util.Random rand = new java.util.Random();
                int endJitterX = rand.nextInt(8) - 4; // -4 to +3 px
                int endJitterY = rand.nextInt(8) - 4;
                end.translate(endJitterX, endJitterY);
                java.util.List<java.awt.Point> path = com.osrs.agent.HumanMousePath.generateBezier(start, end, 20);
                for (java.awt.Point p : path) {
                    java.awt.event.MouseEvent synthetic = new java.awt.event.MouseEvent(
                        component,
                        java.awt.event.MouseEvent.MOUSE_MOVED,
                        System.currentTimeMillis(),
                        0,
                        p.x,
                        p.y,
                        0,
                        false
                    );
                    self.getClass().getMethod("processMouseMoved", java.awt.event.MouseEvent.class)
                        .invoke(self, synthetic);
                    Thread.sleep(5 + (int)(Math.random() * 10));
                }
                // Simulate a click at the end point
                java.awt.event.MouseEvent clickEvent = new java.awt.event.MouseEvent(
                    component,
                    java.awt.event.MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    end.x,
                    end.y,
                    1,
                    false,
                    java.awt.event.MouseEvent.BUTTON1
                );
                self.getClass().getMethod("processMousePressed", java.awt.event.MouseEvent.class)
                    .invoke(self, clickEvent);
                java.awt.event.MouseEvent releaseEvent = new java.awt.event.MouseEvent(
                    component,
                    java.awt.event.MouseEvent.MOUSE_RELEASED,
                    System.currentTimeMillis(),
                    0,
                    end.x,
                    end.y,
                    1,
                    false,
                    java.awt.event.MouseEvent.BUTTON1
                );
                self.getClass().getMethod("processMouseReleased", java.awt.event.MouseEvent.class)
                    .invoke(self, releaseEvent);
                System.out.println("[MouseAutomation] Injected synthetic MouseEvent path and click at " + end);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Advice for per-tick automation logic.
     */
    public static class GameTickAutomationAdvice {
        private static volatile boolean overlayRegistered = false;

        @Advice.OnMethodEnter
        static void onEnter() {
            if (!AgentMain.isAutomationEnabled()) return;
            if (!overlayRegistered) {
                AgentMain.registerOverlay();
                overlayRegistered = true;
            }
            try {
                // Use reflection to get the RuneLite client instance
                Class<?> clientClass = Class.forName("net.runelite.client.RuneLite");
                java.lang.reflect.Field injectorField = clientClass.getDeclaredField("injector");
                injectorField.setAccessible(true);
                Object injector = injectorField.get(null);
                if (injector == null) return;

                // Get the Client instance from the injector
                Object client = injector.getClass().getMethod("getInstance", Class.class)
                    .invoke(injector, Class.forName("net.runelite.api.Client"));
                if (client == null) return;

                // Get local player
                Object player = client.getClass().getMethod("getLocalPlayer").invoke(client);
                if (player == null) return;

                // Get player's world location
                Object worldLocation = player.getClass().getMethod("getWorldLocation").invoke(player);
                int playerX = (int) worldLocation.getClass().getMethod("getX").invoke(worldLocation);
                int playerY = (int) worldLocation.getClass().getMethod("getY").invoke(worldLocation);

                // Detect if player has fallen off the course (placeholder logic)
                if (hasFallenOffCourse(player, worldLocation)) {
                    // Example: Set course start location (replace with actual course start for each course)
                    int startX = 3267; // Example: Seers' Village start X
                    int startY = 3488; // Example: Seers' Village start Y
                    navigateToCourseStart(client, startX, startY);
                    System.out.println("[AgilityAutomation] Detected fall, navigating back to course start");
                    return;
                }

                // Get all game objects (tiles)
                Object scene = client.getClass().getMethod("getScene").invoke(client);
                if (scene == null) return;
                Object[][][] tiles = (Object[][][]) scene.getClass().getMethod("getTiles").invoke(scene);
                if (tiles == null) return;

                // For each tile, check for agility obstacles
                for (Object[][] plane : tiles) {
                    for (Object[] row : plane) {
                        for (Object tile : row) {
                            if (tile == null) continue;
                            Object[] gameObjects = (Object[]) tile.getClass().getMethod("getGameObjects").invoke(tile);
                            if (gameObjects == null) continue;
                            for (Object obj : gameObjects) {
                                if (obj == null) continue;
                                int id = (int) obj.getClass().getMethod("getId").invoke(obj);
                                // Mark of Grace detection (object ID 13119)
                                if (id == 13119) {
                                    Object objWorldLocation = obj.getClass().getMethod("getWorldLocation").invoke(obj);
                                    int objX = (int) objWorldLocation.getClass().getMethod("getX").invoke(objWorldLocation);
                                    int objY = (int) objWorldLocation.getClass().getMethod("getY").invoke(objWorldLocation);
                                    if (Math.abs(playerX - objX) < 3 && Math.abs(playerY - objY) < 3) {
                                        System.out.println("[AgilityAutomation] Mark of Grace detected at " + objX + "," + objY);
                                        Object localPoint = obj.getClass().getMethod("getLocalLocation").invoke(obj);
                                        int planeObstacle = (int) client.getClass().getMethod("getPlane").invoke(client);
                                        Class<?> perspectiveClass = Class.forName("net.runelite.api.Perspective");
                                        java.awt.Point canvasPoint = (java.awt.Point) perspectiveClass.getMethod(
                                            "localToCanvas", Class.forName("net.runelite.api.Client"),
                                            Class.forName("net.runelite.api.coords.LocalPoint"), int.class)
                                            .invoke(null, client, localPoint, planeObstacle);
                                        if (canvasPoint != null) {
                                            int screenX = canvasPoint.x;
                                            int screenY = canvasPoint.y;
                                            triggerMouseAutomation(screenX, screenY);
                                            System.out.println("[AgilityAutomation] Triggering mouse automation for Mark of Grace at screen coords: " + screenX + ", " + screenY);
                                            return;
                                        }
                                    }
                                }
                                // TODO: Replace with actual agility obstacle IDs from Obstacles.java
                                if (isAgilityObstacle(id)) {
                                    Object objWorldLocation = obj.getClass().getMethod("getWorldLocation").invoke(obj);
                                    int objX = (int) objWorldLocation.getClass().getMethod("getX").invoke(objWorldLocation);
                                    int objY = (int) objWorldLocation.getClass().getMethod("getY").invoke(objWorldLocation);
                                    // If player is close enough, trigger mouse automation
                                    if (Math.abs(playerX - objX) < 3 && Math.abs(playerY - objY) < 3) {
                                        System.out.println("[AgilityAutomation] Found obstacle at " + objX + "," + objY + " (id=" + id + ")");
                                        // Convert world location to screen coordinates using Perspective.localToCanvas
                                        // This requires the client, the object's local location, and the current plane
                                        Object localPoint = obj.getClass().getMethod("getLocalLocation").invoke(obj);
                                        int plane2 = (int) client.getClass().getMethod("getPlane").invoke(client);
                                        Class<?> perspectiveClass = Class.forName("net.runelite.api.Perspective");
                                        java.awt.Point canvasPoint = (java.awt.Point) perspectiveClass.getMethod(
                                            "localToCanvas", Class.forName("net.runelite.api.Client"),
                                            Class.forName("net.runelite.api.coords.LocalPoint"), int.class)
                                            .invoke(null, client, localPoint, plane2);
                                        if (canvasPoint != null) {
                                            int screenX = canvasPoint.x;
                                            int screenY = canvasPoint.y;
                                            triggerMouseAutomation(screenX, screenY);
                                            System.out.println("[AgilityAutomation] Triggering mouse automation at screen coords: " + screenX + ", " + screenY);
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Placeholder: Detect if player has fallen off the course
        private static boolean hasFallenOffCourse(Object player, Object worldLocation) {
            try {
                // 1. Animation-based detection (fall animation for agility is 827)
                int animation = (int) player.getClass().getMethod("getAnimation").invoke(player);
                if (animation == 827) {
                    System.out.println("[AgilityAutomation] Detected fall animation.");
                    return true;
                }
                // 2. Rooftop whitelist: Only allow known Canifis rooftop tiles
                int x = (int) worldLocation.getClass().getMethod("getX").invoke(worldLocation);
                int y = (int) worldLocation.getClass().getMethod("getY").invoke(worldLocation);
                int z = (int) worldLocation.getClass().getMethod("getPlane").invoke(worldLocation);
                if (z != 1) {
                    // Not on rooftop plane
                    System.out.println("[AgilityAutomation] Player not on rooftop plane.");
                    return true;
                }
                // Canifis rooftop tile whitelist (bounding box for simplicity)
                if (x < 3495 || x > 3520 || y < 3465 || y > 3500) {
                    System.out.println("[AgilityAutomation] Player outside Canifis rooftop bounds.");
                    return true;
                }
                // 3. (Optional) Add chat message detection for falls if needed
            } catch (Exception ignored) {}
            return false;
        }

        // Simulate navigation to course start
        private static void navigateToCourseStart(Object client, int startX, int startY) {
            // Canifis course start tile: (3508, 3488, 0)
            startX = 3508;
            startY = 3488;
            // Convert world coordinates to screen coordinates
            try {
                Class<?> worldPointClass = Class.forName("net.runelite.api.coords.WorldPoint");
                Object startPoint = worldPointClass.getConstructor(int.class, int.class, int.class)
                    .newInstance(startX, startY, 0);
                Object localPoint = worldPointClass.getMethod("toLocalInstance", Class.forName("net.runelite.api.Client"), boolean.class)
                    .invoke(startPoint, client, false);
                int planeStart = 0;
                Class<?> perspectiveClass = Class.forName("net.runelite.api.Perspective");
                java.awt.Point canvasPoint = (java.awt.Point) perspectiveClass.getMethod(
                    "localToCanvas", Class.forName("net.runelite.api.Client"),
                    Class.forName("net.runelite.api.coords.LocalPoint"), int.class)
                    .invoke(null, client, localPoint, planeStart);
                if (canvasPoint != null) {
                    triggerMouseAutomation(canvasPoint.x, canvasPoint.y);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Example: Replace with actual obstacle ID check
        private static boolean isAgilityObstacle(int id) {
            // Canifis rooftop course obstacle IDs (from RuneLite Obstacles.java / OSRS Wiki)
            int[] canifisObstacleIds = {
                10819, // Tall tree
                10820, // Gap (first)
                10821, // Gap (second)
                10828, // Gap (third)
                10822, // Gap (fourth)
                10831, // Pole-vault
                10823, // Gap (fifth)
                10830  // Gap (final)
            };
            for (int oid : canifisObstacleIds) {
                if (id == oid) return true;
            }
            return false;
        }

        // Trigger mouse automation at the given screen coordinates
        private static void triggerMouseAutomation(int screenX, int screenY) {
            // Set the target for MouseAutomationAdvice to use on the next mouse event
            MouseAutomationAdvice.setTarget(screenX, screenY);
            System.out.println("[AgilityAutomation] Triggering mouse automation at screen coords: " + screenX + ", " + screenY);
        }
    }

    /**
     * Transformer for game tick hooks.
     */
    public static class GameTickHook implements AgentBuilder.Transformer {
        @Override
        public net.bytebuddy.dynamic.DynamicType.Builder<?> transform(
                net.bytebuddy.dynamic.DynamicType.Builder<?> builder,
                net.bytebuddy.description.type.TypeDescription typeDescription,
                ClassLoader classLoader,
                net.bytebuddy.utility.JavaModule module,
                java.security.ProtectionDomain protectionDomain) {
            return builder.visit(Advice.to(GameTickAutomationAdvice.class).on(ElementMatchers.named("tick")));
        }
    }
}
