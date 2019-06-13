package io.github.zekerzhayard.noserverargument.launch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class VersionJsonReader {
    public static List<URL> getLibraries(String minecraftVersion) {
        List<URL> libraries = new ArrayList<>();
        try {
            JsonArray jsonArray = new JsonParser().parse(FileUtils.readFileToString(new File("./versions/" + minecraftVersion + "/" + minecraftVersion + ".json"), "UTF-8")).getAsJsonObject().get("libraries").getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                URL url = VersionJsonReader.getLibraryURL(jsonArray.get(i));
                if (url != null) {
                    libraries.add(url);
                    System.out.println("Successfully add a url: " + url);
                } else {
                    System.out.println("The url is null!");
                }
            }
            String selfJarPath = StringUtils.substringAfter(URLDecoder.decode(VersionJsonReader.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8").split("!/")[0], "/");
            System.out.println("Add self jar file: " + selfJarPath);
            libraries.add(new File(selfJarPath).toURI().toURL());
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
        return libraries;
    }
    
    private static URL getLibraryURL(JsonElement jsonElement) {
        String[] libStrs = jsonElement.getAsJsonObject().get("name").getAsString().split(":");
        if (libStrs.length == 3) {
            String libPath = "./libraries/" + libStrs[0].replace(".", "/") + "/" + libStrs[1] + "/" + libStrs[2] + "/" + libStrs[1] + "-" + libStrs[2] + ".jar";
            if (libPath.equals("./libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar")) {
                return null;
            }
            try {
                return new File(libPath).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
