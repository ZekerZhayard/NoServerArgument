package io.github.zekerzhayard.noserverargument.launch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public class NewMinecraftLauncher extends Thread {
    private String[] args;
    private NewLaunchClassLoader classLoader;
    
    public NewMinecraftLauncher(String minecraftVersion, String[] args) {
        this.args = ArrayUtils.addAll(args, "--tweakClass", "net.minecraftforge.fml.common.launcher.FMLTweaker");
        List<URL> urls = VersionJsonReader.getLibraries(minecraftVersion);
        try {
            urls.add(new File("./versions/" + minecraftVersion + "/" + minecraftVersion + ".jar").toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.classLoader = new NewLaunchClassLoader(urls.toArray(new URL[0]));
        this.classLoader.registerTransformer("io.github.zekerzhayard.noserverargument.launch.asm.SecurityBreaker");
    }
    
    @Override
    public void run() {
        try {
            Class<?> mainClass = this.classLoader.loadClass("net.minecraft.launchwrapper.Launch");
            MethodUtils.invokeExactStaticMethod(mainClass, "main", new Object[] { this.args });
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
