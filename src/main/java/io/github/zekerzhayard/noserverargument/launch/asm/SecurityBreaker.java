package io.github.zekerzhayard.noserverargument.launch.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SecurityBreaker implements IClassTransformer {
    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (className.equals("net.minecraftforge.fml.common.launcher.FMLTweaker")) {
            System.out.println("Found the class: " + className);
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, ClassReader.EXPAND_FRAMES);
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("<init>") && mn.desc.equals("()V")) {
                    System.out.println("Found the method: " + mn.name + mn.desc);
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            if (min.owner.equals("java/lang/System") && min.name.equals("setSecurityManager") && min.desc.equals("(Ljava/lang/SecurityManager;)V")) {
                                System.out.println("Found the insn: " + min.owner + "." + min.name + min.desc);
                                mn.instructions.insert(min, new InsnNode(Opcodes.POP));
                                mn.instructions.remove(min);
                            }
                        }
                    }
                }
            }
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();
        }
        return basicClass;
    }
}
