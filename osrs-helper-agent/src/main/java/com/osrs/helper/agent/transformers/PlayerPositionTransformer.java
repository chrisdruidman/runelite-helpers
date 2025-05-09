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
        //System.out.println("[DEBUG] PlayerPositionTransformer: transform called for class: " + className);
        // Only transform the obfuscated Player class (update this name as needed)
        if (!className.equals("client/Player")) {
            return null;
        }
        //System.out.println("[DEBUG] PlayerPositionTransformer: matched class client/Player");
        // Use ASM to modify the class bytecode
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                //System.out.println("[DEBUG] PlayerPositionTransformer: method in class " + className + ": " + name + desc);
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                // Example: look for the setPosition method (update name/desc as needed)
                if (name.equals("setPosition") && desc.equals("(III)V")) {
                    System.out.println("[DEBUG] PlayerPositionTransformer: matched method setPosition(III)V in class " + className);
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                // Inject: new WorldPosition(x, y, plane) and call HookingService.onPlayerPositionChanged
                                // Load 'this' (Player instance) to get x, y, plane fields
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "x", "I");
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "y", "I");
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "plane", "I");
                                // Construct new WorldPosition(x, y, plane)
                                super.visitTypeInsn(Opcodes.NEW, "com/osrs/helper/agent/helpermodules/agility/WorldPosition");
                                super.visitInsn(Opcodes.DUP);
                                // Push x, y, plane for constructor
                                super.visitVarInsn(Opcodes.ILOAD, 1); // x
                                super.visitVarInsn(Opcodes.ILOAD, 2); // y
                                super.visitVarInsn(Opcodes.ILOAD, 3); // plane
                                super.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/osrs/helper/agent/helpermodules/agility/WorldPosition", "<init>", "(III)V", false);
                                // Call static hook
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/osrs/helper/agent/services/HookingService",
                                    "onPlayerPositionChanged",
                                    "(Ljava/lang/Object;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                else {
                    System.out.println("[DEBUG] PlayerPositionTransformer: matched method setPosition(III)V in class " + className);
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
