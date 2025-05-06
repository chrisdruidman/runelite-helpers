package com.osrs.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[OSRS Helper Agent] Agent started. Initializing ByteBuddy...");
        // Install generic reusable hook
        new AgentBuilder.Default()
            .type(ElementMatchers.nameContainsIgnoreCase("runelite"))
            .transform(new GenericHook(ElementMatchers.any()))
            .type(ElementMatchers.named("net.runelite.client.input.MouseManager"))
            .transform(new MouseAutomationHook())
            .type(ElementMatchers.named("net.runelite.client.callback.Hooks"))
            .transform(new GameTickHook())
            .installOn(inst);
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
                java.lang.module.Module module) {
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
                java.lang.module.Module module) {
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
        @Advice.OnMethodEnter
        static void onEnter() {
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
                                        int plane = (int) client.getClass().getMethod("getPlane").invoke(client);
                                        Class<?> perspectiveClass = Class.forName("net.runelite.api.Perspective");
                                        java.awt.Point canvasPoint = (java.awt.Point) perspectiveClass.getMethod(
                                            "localToCanvas", Class.forName("net.runelite.api.Client"),
                                            Class.forName("net.runelite.api.coords.LocalPoint"), int.class)
                                            .invoke(null, client, localPoint, plane);
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
                                        int plane = (int) client.getClass().getMethod("getPlane").invoke(client);
                                        Class<?> perspectiveClass = Class.forName("net.runelite.api.Perspective");
                                        java.awt.Point canvasPoint = (java.awt.Point) perspectiveClass.getMethod(
                                            "localToCanvas", Class.forName("net.runelite.api.Client"),
                                            Class.forName("net.runelite.api.coords.LocalPoint"), int.class)
                                            .invoke(null, client, localPoint, plane);
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

        // Example: Replace with actual obstacle ID check
        private static boolean isAgilityObstacle(int id) {
            // TODO: Use real obstacle IDs from Obstacles.java
            int[] exampleObstacleIds = { 10093, 10094, 10095, 10096, 10097, 10098 };
            for (int oid : exampleObstacleIds) {
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
                java.lang.module.Module module) {
            return builder.visit(Advice.to(GameTickAutomationAdvice.class).on(ElementMatchers.named("tick")));
        }
    }
}
