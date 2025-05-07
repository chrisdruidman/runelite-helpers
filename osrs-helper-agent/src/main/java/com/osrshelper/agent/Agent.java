package com.osrshelper.agent;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import net.runelite.api.Client;
import net.runelite.client.input.MouseManager;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

public class Agent {
    private static final String LOG_FILE = "agent-output";
    private static final Logger logger = Logger.getLogger("AgentLogger");
    private static final Map<String, HelperModule> modules = new HashMap<>();
    private static Instrumentation instrumentationRef;
    private static final AtomicReference<Object> runeliteInstanceRef = new AtomicReference<>();
    private static final AtomicReference<Object> mouseManagerInstanceRef = new AtomicReference<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentationRef = inst;
        setupLogging();
        logger.info("Agent started at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        inst.addTransformer(new RuneLiteConstructorTransformer(), false);
        inst.addTransformer(new MouseManagerConstructorTransformer(), false);
        // No background thread: logic will run after RuneLite instance is constructed
    }

    // Called from injected bytecode in RuneLite constructor
    public static void onRuneLiteConstructed(Object instance) {
        runeliteInstanceRef.set(instance);
        logger.info("RuneLite instance captured via constructor instrumentation.");
        runAgentLogic();
    }

    // Called from injected bytecode in MouseManager constructor
    public static void onMouseManagerConstructed(Object instance) {
        mouseManagerInstanceRef.set(instance);
        logger.info("MouseManager instance captured via constructor instrumentation.");
    }

    private static void runAgentLogic() {
        new Thread(() -> {
            try {
                Class<?> runeliteClass = Class.forName("net.runelite.client.RuneLite");
                Object runeliteInstance = findRuneLiteInstance(runeliteClass);
                if (runeliteInstance == null) {
                    logger.severe("Could not find RuneLite main instance via reflection.");
                    return;
                }
                Field clientField = runeliteClass.getDeclaredField("client");
                clientField.setAccessible(true);
                Client client = null;
                // Wait for the client field to become non-null (max 2 minutes)
                for (int i = 0; i < 1200; i++) {
                    client = (Client) clientField.get(runeliteInstance);
                    if (client != null) break;
                    if (i % 10 == 0) {
                        logger.info("Waiting for RuneLite client field to be initialized... (" + (i/10) + "s)");
                    }
                    Thread.sleep(100);
                }
                if (client == null) {
                    logger.severe("Could not extract Client from RuneLite instance (field was null after waiting)." );
                    return;
                }
                logger.info("Successfully found RuneLite Client instance via reflection.");
                // Find MouseManager instance via reflection
                Object mouseManagerInstance = null;
                // Wait for MouseManager to become available (max 2 minutes)
                for (int i = 0; i < 1200; i++) {
                    mouseManagerInstance = mouseManagerInstanceRef.get();
                    if (mouseManagerInstance != null) break;
                    if (i % 10 == 0) {
                        logger.info("Waiting for MouseManager instance to be constructed... (" + (i/10) + "s)");
                    }
                    Thread.sleep(100);
                }
                if (mouseManagerInstance == null) {
                    logger.severe("Could not find MouseManager instance via constructor instrumentation after waiting.");
                    return;
                }
                logger.info("Successfully found MouseManager instance via constructor instrumentation.");
                // Instantiate the real game state provider
                RealGameStateProvider gameStateProvider = new RealGameStateProvider(client);
                // Create ServiceRegistry and register services
                ServiceRegistry serviceRegistry = new ServiceRegistry();
                serviceRegistry.register(Client.class, client);
                serviceRegistry.register(RealGameStateProvider.class, gameStateProvider);
                serviceRegistry.register(MouseManager.class, (MouseManager) mouseManagerInstance);
                // Create MouseInputService with all dependencies
                MouseInputService mouseInputService = new MouseInputService(serviceRegistry);
                serviceRegistry.register(MouseInputService.class, mouseInputService);
                // Register modules
                modules.put("agility", new AgilityModule(serviceRegistry));
                // Launch overlay for toggling modules
                OverlayController overlay = new OverlayController(modules);
                overlay.show();
                // Example: run the Agility module if enabled
                if (overlay.isModuleEnabled("agility")) {
                    runModule("agility");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Agent initialization failed (runAgentLogic)", e);
            }
        }, "AgentClientWaitThread").start();
    }

    // Utility: Find the RuneLite main instance by scanning all loaded objects (agent trick)
    private static Object findRuneLiteInstance(Class<?> runeliteClass) {
        Object instance = runeliteInstanceRef.get();
        if (instance != null) {
            logger.info("Found RuneLite instance via constructor instrumentation.");
            return instance;
        }
        // Try to enumerate all live objects using Instrumentation
        try {
            if (instrumentationRef != null) {
                for (Class<?> clazz : instrumentationRef.getAllLoadedClasses()) {
                    if (runeliteClass.isAssignableFrom(clazz)) {
                        // Try to enumerate all instances of this class
                        // Use instrumentation to get all instances (Java 9+ has no public API, but some agents use JVMTI or Unsafe)
                        // Here, we use a workaround: force a heap dump and scan, or use a known agent library (not implemented here)
                        // For now, try to find via static field search as fallback
                        // (If you want a full heap scan, consider using an external agent library)
                    }
                }
            }
            // Fallback: try static field search (legacy, may not work)
            for (Field field : runeliteClass.getDeclaredFields()) {
                if (runeliteClass.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object instance2 = field.get(null);
                    if (instance2 != null) {
                        logger.info("Found RuneLite instance via static field: " + field.getName());
                        return instance2;
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to find RuneLite instance via Instrumentation/static field", e);
        }
        logger.warning("Could not find RuneLite instance via Instrumentation or static field. Consider using a JVMTI heap scan for more robustness.");
        return null;
    }

    // Utility: Find MouseManager in all fields (including private and inherited) of the RuneLite instance
    private static Object findMouseManager(Class<?> runeliteClass, Object runeliteInstance) {
        try {
            Class<?> clazz = runeliteClass;
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (MouseManager.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object value = field.get(runeliteInstance);
                        if (value != null) {
                            logger.info("Found MouseManager in field: " + field.getName());
                            return value;
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error searching for MouseManager via reflection", e);
        }
        return null;
    }

    private static void setupLogging() {
        try {
            // Clear the log file at the start of each run
            File logFile = new File(LOG_FILE);
            if (logFile.exists()) {
                new PrintWriter(logFile).close();
            }
            // Set up file handler
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            // Set up console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            // Configure logger
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to set up logging: " + e.getMessage());
        }
    }

    public static void registerModule(HelperModule module) {
        modules.put(module.getName().toLowerCase(), module);
        logger.info("Registered module: " + module.getName());
    }

    public static HelperModule getModule(String name) {
        return modules.get(name.toLowerCase());
    }

    public static void runModule(String name) {
        HelperModule module = getModule(name);
        if (module != null) {
            logger.info("Running module: " + name);
            module.run();
        } else {
            logger.warning("Module not found: " + name);
        }
    }

    // Bytecode transformer to inject a call to Agent.onRuneLiteConstructed(this) in the RuneLite constructor
    private static class RuneLiteConstructorTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if (!"net/runelite/client/RuneLite".equals(className)) {
                return null;
            }
            try {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                        if ("<init>".equals(name)) {
                            return new AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
                                @Override
                                protected void onMethodExit(int opcode) {
                                    // Inject Agent.onRuneLiteConstructed(this) at the end of the constructor
                                    mv.visitVarInsn(Opcodes.ALOAD, 0); // load 'this'
                                    mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "com/osrshelper/agent/Agent",
                                        "onRuneLiteConstructed",
                                        "(Ljava/lang/Object;)V",
                                        false
                                    );
                                }
                            };
                        }
                        return mv;
                    }
                };
                cr.accept(cv, 0);
                logger.info("Injected Agent.onRuneLiteConstructed(this) into RuneLite constructor using ASM.");
                return cw.toByteArray();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to instrument RuneLite constructor with ASM", e);
                return null;
            }
        }
    }

    // Bytecode transformer to inject a call to Agent.onMouseManagerConstructed(this) in the MouseManager constructor
    private static class MouseManagerConstructorTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if (!"net/runelite/client/input/MouseManager".equals(className)) {
                return null;
            }
            try {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                        if ("<init>".equals(name)) {
                            return new AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
                                @Override
                                protected void onMethodExit(int opcode) {
                                    // Inject Agent.onMouseManagerConstructed(this) at the end of the constructor
                                    mv.visitVarInsn(Opcodes.ALOAD, 0); // load 'this'
                                    mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "com/osrshelper/agent/Agent",
                                        "onMouseManagerConstructed",
                                        "(Ljava/lang/Object;)V",
                                        false
                                    );
                                }
                            };
                        }
                        return mv;
                    }
                };
                cr.accept(cv, 0);
                logger.info("Injected Agent.onMouseManagerConstructed(this) into MouseManager constructor using ASM.");
                return cw.toByteArray();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to instrument MouseManager constructor with ASM", e);
                return null;
            }
        }
    }
}
