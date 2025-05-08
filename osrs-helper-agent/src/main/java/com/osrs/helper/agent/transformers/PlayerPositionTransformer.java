package com.osrs.helper.agent.transformers;

import org.objectweb.asm.*;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * ASM transformer for injecting a hook into the Player position update logic.
 * This transformer is modular and can be registered in the agent premain.
 */
public class PlayerPositionTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // Only transform the obfuscated Player class (update this name as needed)
        if (!className.equals("client/Player")) {
            return null;
        }
        // Use ASM to modify the class bytecode
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                // Example: look for the setPosition method (update name/desc as needed)
                if (name.equals("setPosition") && desc.equals("(III)V")) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                // Inject: HookingService.onPlayerPositionChanged(new WorldPoint(x, y, plane));
                                // (You may need to construct a WorldPoint or pass x, y, plane directly)
                                super.visitVarInsn(Opcodes.ALOAD, 0); // 'this'
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "x", "I");
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "y", "I");
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "plane", "I");
                                // Call static hook (update signature as needed)
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/osrs/helper/agent/services/HookingService",
                                    "onPlayerPositionChanged",
                                    "(Ljava/lang/Object;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
