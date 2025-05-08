package com.osrs.helper.agent.transformers;

import org.objectweb.asm.*;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * ASM transformer for injecting a hook into the Player animation field/method.
 * This transformer is modular and can be registered in the agent premain.
 */
public class PlayerAnimationTransformer implements ClassFileTransformer {
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
                // Example: look for the setAnimation method (update name/desc as needed)
                if (name.equals("setAnimation") && desc.equals("(I)V")) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                // Inject: HookingService.onPlayerAnimationChanged(animation != -1);
                                super.visitVarInsn(Opcodes.ALOAD, 0); // 'this'
                                super.visitFieldInsn(Opcodes.GETFIELD, "client/Player", "animation", "I");
                                super.visitInsn(Opcodes.ICONST_M1);
                                Label notAnimating = new Label();
                                super.visitJumpInsn(Opcodes.IF_ICMPEQ, notAnimating);
                                super.visitInsn(Opcodes.ICONST_1);
                                Label callHook = new Label();
                                super.visitJumpInsn(Opcodes.GOTO, callHook);
                                super.visitLabel(notAnimating);
                                super.visitInsn(Opcodes.ICONST_0);
                                super.visitLabel(callHook);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/osrs/helper/agent/services/HookingService",
                                    "onPlayerAnimationChanged",
                                    "(Z)V", false);
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
