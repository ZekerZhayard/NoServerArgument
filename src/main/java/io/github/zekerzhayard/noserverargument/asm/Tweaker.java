package io.github.zekerzhayard.noserverargument.asm;

import java.io.File;
import java.util.List;

import io.github.zekerzhayard.noserverargument.asm.util.LogFileUtil;
import net.minecraft.client.main.Main;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Tweaker implements ITweaker {
    static {
        LogFileUtil.gzipLatestLog(new File("./logs").getAbsolutePath());
    }
    
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer(ClassTransformer.class.getName());
    }

    @Override
    public String getLaunchTarget() {
        return Main.class.getName();
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
