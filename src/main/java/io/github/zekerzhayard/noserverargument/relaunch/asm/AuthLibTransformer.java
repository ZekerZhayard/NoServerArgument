package io.github.zekerzhayard.noserverargument.relaunch.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class AuthLibTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (className.equals("com.mojang.authlib.AuthenticationCpp")) {
            System.out.println("[Relauncher] Found the class: " + className);
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, ClassReader.EXPAND_FRAMES);
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("LoadLibrary") && mn.desc.equals("()Ljava/lang/Boolean;")) {
                    System.out.println("[Relauncher] Found the method: " + mn.name + mn.desc);
                    InsnList insnList = new InsnList();
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "AuthLibHook", "loadLibrary", "()Ljava/lang/Boolean;", false));
                    insnList.add(new InsnNode(Opcodes.ARETURN));
                    mn.instructions.insert(insnList);
                } else if (mn.name.equals("Authentication") && mn.desc.equals("(ILjava/lang/String;)Ljava/lang/Boolean;")) {
                    System.out.println("[Relauncher] Found the method: " + mn.name + mn.desc);
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "AuthLibHook", "authentication", "(ILjava/lang/String;)Ljava/lang/Boolean;", false));
                    insnList.add(new InsnNode(Opcodes.ARETURN));
                    mn.instructions.insert(insnList);
                }
            }
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();
        }
        return basicClass;
    }
}
