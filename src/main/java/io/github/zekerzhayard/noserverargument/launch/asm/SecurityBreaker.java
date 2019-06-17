package io.github.zekerzhayard.noserverargument.launch.asm;

import java.util.HashSet;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SecurityBreaker implements IClassTransformer {
    private HashSet<String> transformerClasses = new HashSet<>();
    
    public SecurityBreaker() {
        this.transformerClasses.add("net.minecraftforge.fml.common.launcher.FMLTweaker");
        this.transformerClasses.add("net.minecraftforge.fml.relauncher.FMLLaunchHandler");
    }
    
    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (this.transformerClasses.contains(className)) {
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
                                mn.instructions.insertBefore(min, new InsnNode(Opcodes.POP));
                                mn.instructions.remove(min);
                            }
                        }
                    }
                } else if (mn.name.equals("<init>") && mn.desc.equals("(Lnet/minecraft/launchwrapper/LaunchClassLoader;Lnet/minecraftforge/fml/common/launcher/FMLTweaker;)V")) {
                    System.out.println("Found the method: " + mn.name + mn.desc);
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof LdcInsnNode) {
                            LdcInsnNode lin = (LdcInsnNode) ain;
                            if (lin.cst.equals("org.objectweb.asm.")) {
                                System.out.println("Found the insn: " + lin.cst);
                                mn.instructions.remove(lin.getNext());
                                mn.instructions.insert(lin, new InsnNode(Opcodes.POP2));
                            }
                        }
                    }
                } else if (mn.name.equals("injectIntoClassLoader") && mn.desc.equals("(Lnet/minecraft/launchwrapper/LaunchClassLoader;)V")) {
                    System.out.println("Found the method: " + mn.name + mn.desc);
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "NewLauncherHook", "injectIntoClassLoader", "(Lnet/minecraft/launchwrapper/LaunchClassLoader;)V", false));
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
