package com.osrs.helper.agent.transformers;

import org.objectweb.asm.*;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * ASM transformer for injecting a hook into the scene object presence logic.
 * This transformer is modular and can be registered in the agent premain.
 */
public class ObjectPresenceTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // Only transform the obfuscated Scene or Tile class (update this name as needed)
        if (!className.equals("client/Tile")) {
            return null;
        }
        // Use ASM to modify the class bytecode
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                // Example: look for the setGameObject method (update name/desc as needed)
                if (name.equals("setGameObject") && desc.equals("(Lclient/GameObject;I)V")) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                // Inject: HookingService.onObjectPresenceChanged(objectId, present);
                                // (You may need to extract the objectId and presence flag from method args or fields)
                                super.visitVarInsn(Opcodes.ALOAD, 1); // GameObject
                                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "client/GameObject", "getId", "()I", false);
                                // Convert int to String if needed, or pass as int
                                // For presence, assume true for add, false for remove (update as needed)
                                super.visitInsn(Opcodes.ICONST_1); // true for add
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/osrs/helper/agent/services/HookingService",
                                    "onObjectPresenceChanged",
                                    "(Ljava/lang/String;Z)V", false);
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
