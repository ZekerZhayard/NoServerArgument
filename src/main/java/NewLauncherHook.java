import net.minecraft.launchwrapper.LaunchClassLoader;

public class NewLauncherHook {
    public static void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("com.netease.mc.mod.network.");
        classLoader.registerTransformer("io.github.zekerzhayard.noserverargument.relaunch.asm.AuthLibTransformer");
    }
}
