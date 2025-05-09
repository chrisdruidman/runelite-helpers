package com.osrs.helper.agent.services;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import java.io.IOException;

/**
 * Service for injecting the Java Swing overlay into the RuneLite client.
 * <b>IMPORTANT:</b> This is the ONLY service that uses injected hooks/ASM. All other agent logic must use the minimal API exposed by patch files only.
 * Do NOT reference or depend on any code from runelite/ directly. This service is part of the hybrid patch-based approach.
 *
 * The ASM injection points must be maintained in patch files and documented in osrs-helper-patches/.
 */
public class OverlayInjectionService implements AgentService {
    private static final Logger logger = Logger.getLogger("OverlayInjectionService");
    private Instrumentation instrumentation;

    @Override
    public void initialize() {
        logger.info("OverlayInjectionService initialized");
        if (instrumentation == null) {
            logger.warning("Instrumentation is not set. ASM injection will not proceed.");
            return;
        }
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if ("net/runelite/client/RuneLite".equals(className)) {
                    logger.info("RuneLite main class detected. Attempting ASM injection.");
                    try {
                        // NOTE: All injected logic must be documented in patch files and use only the minimal API.
                        return injectOverlayLaunch(classfileBuffer);
                    } catch (Exception e) {
                        logger.severe("ASM injection failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }, true);
    }

    /**
     * Uses ASM to inject overlay launch logic after clientUI.setVisible(true) in RuneLite.main().
     * <b>NOTE:</b> This must be kept in sync with the patch files in osrs-helper-patches/.
     * Do not reference runelite/ code directly.
     */
    private byte[] injectOverlayLaunch(byte[] classfileBuffer) throws IOException {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("main".equals(name) && "([Ljava/lang/String;)V".equals(descriptor)) {
                    return new AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
                        private boolean injected = false;
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                            // Look for clientUI.setVisible(true)
                            if (!injected && opcode == Opcodes.INVOKEVIRTUAL && "setVisible".equals(name) && "(Z)V".equals(desc)) {
                                // Inject: OsrsHelperAgent.launchOverlay();
                                visitMethodInsn(
                                    Opcodes.INVOKESTATIC,
                                    "com/osrs/helper/agent/OsrsHelperAgent",
                                    "launchOverlay",
                                    "()V",
                                    false
                                );
                                injected = true;
                            }
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    @Override
    public void shutdown() {
        logger.info("OverlayInjectionService shutdown");
        // TODO: Clean up any hooks or resources if needed
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }
}
