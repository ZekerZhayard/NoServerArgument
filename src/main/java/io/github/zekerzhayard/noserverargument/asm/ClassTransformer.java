package io.github.zekerzhayard.noserverargument.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        System.out.println("Found the class: " + className + " -> " + transformedName);
        if (transformedName.equals("net.minecraft.client.main.Main") || transformedName.equals("com.netease.mc.mod.friendplay.GuiOpenEventHandler")) {
            System.out.println("Found the class: " + className + " -> " + transformedName);
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, ClassReader.EXPAND_FRAMES);
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V")) {
                    System.out.println("Found the method: " + mn.name + mn.desc);
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "NoServerArgumentHook", "removeServerArgs", "([Ljava/lang/String;)[Ljava/lang/String;", false));
                    insnList.add(new VarInsnNode(Opcodes.ASTORE, 0));
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    mn.instructions.insert(insnList);
                } else if (mn.name.equals("MainMenuOpen") && mn.desc.equals("(Lnet/minecraftforge/client/event/GuiOpenEvent;)V")) {
                    System.out.println("Found the method: " + mn.name + mn.desc);
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            if (min.owner.equals("net/minecraft/client/Minecraft") && min.name.equals("func_71400_g") && min.desc.equals("()V")) {
                                System.out.println("Found the insn: " + min.owner + "." + min.name + min.desc);
                                mn.instructions.insert(min, new InsnNode(Opcodes.POP));
                                mn.instructions.remove(min);
                            } else if (min.owner.equals("com/netease/mc/mod/network/message/request/MessageRequest") && min.name.equals("send") && min.desc.equals("(I[Ljava/lang/Object;)V")) {
                                System.out.println("Found the insn: " + min.owner + "." + min.name + min.desc);
                                mn.instructions.insert(min, new InsnNode(Opcodes.POP2));
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
