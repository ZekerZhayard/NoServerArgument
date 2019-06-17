import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.launchwrapper.LaunchClassLoader;

public class NewLauncherHook {
    public static void injectIntoClassLoader(LaunchClassLoader classLoader) {
        // Ensure that netease mods will not be loaded.
        classLoader.addClassLoaderExclusion("com.netease.mc.mod.");
        classLoader.addClassLoaderExclusion("io.github.zekerzhayard.noserverargument.launch.");
        classLoader.registerTransformer("io.github.zekerzhayard.noserverargument.relaunch.asm.AuthLibTransformer");
        
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d\\.log");
        for (File logFiles : new File("./logs").listFiles()) {
            Matcher matcher = pattern.matcher(logFiles.getName());
            if (matcher.find() && matcher.group().equals(logFiles.getName())) {
                logFiles.delete();
            }
        }
    }
}
