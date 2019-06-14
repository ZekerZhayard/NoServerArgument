package io.github.zekerzhayard.noserverargument.launch;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class NewLaunchClassLoader extends LaunchClassLoader {
    private ClassLoader parent = (ClassLoader) this.readField("parent");
    private ClassLoader boot = this.getClass().getClassLoader();
    
    @SuppressWarnings("unchecked")
    private Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>) this.readField("cachedClasses");
    
    @SuppressWarnings("unchecked")
    private Set<String> invalidClasses = (Set<String>) this.readField("invalidClasses");
    
    @SuppressWarnings("unchecked")
    private Set<String> classLoaderExceptions = (Set<String>) this.readField("classLoaderExceptions");
    
    @SuppressWarnings("unchecked")
    private Set<String> transformerExceptions = (Set<String>) this.readField("transformerExceptions");
    
    private static final boolean DEBUG = (boolean) NewLaunchClassLoader.readStaticField("DEBUG");
    
    private static final boolean DEBUG_SAVE = (boolean) NewLaunchClassLoader.readStaticField("DEBUG_SAVE");
    
    public NewLaunchClassLoader(URL[] sources) {
        super(sources);
        this.classLoaderExceptions.clear();
        this.addClassLoaderExclusion("io.github.zekerzhayard.noserverargument.launch.");
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        if (this.invalidClasses.contains(name)) {
            throw new ClassNotFoundException(name);
        }
        
        if (name.startsWith("com.mojang.authlib.AuthenticationCpp") || name.startsWith("com.netease.mc.mod.network.")) {
            return this.boot.loadClass(name);
        }

        for (final String exception : this.classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return this.parent.loadClass(name);
            }
        }

        if (this.cachedClasses.containsKey(name)) {
            return this.cachedClasses.get(name);
        }

        for (final String exception : this.transformerExceptions) {
            if (name.startsWith(exception)) {
                try {
                    final Class<?> clazz = super.findClass(name);
                    this.cachedClasses.put(name, clazz);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    this.invalidClasses.add(name);
                    throw e;
                }
            }
        }

        try {
            final String transformedName = this.transformName(name);
            if (this.cachedClasses.containsKey(transformedName)) {
                return this.cachedClasses.get(transformedName);
            }

            final String untransformedName = this.untransformName(name);

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            final String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = this.findCodeSourceConnectionFor(fileName);

            CodeSigner[] signers = null;

            if (lastDot > -1 && !untransformedName.startsWith("net.minecraft.")) {
                if (urlConnection instanceof JarURLConnection) {
                    final JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                    final JarFile jarFile = jarURLConnection.getJarFile();

                    if (jarFile != null && jarFile.getManifest() != null) {
                        final Manifest manifest = jarFile.getManifest();
                        final JarEntry entry = jarFile.getJarEntry(fileName);

                        Package pkg = getPackage(packageName);
                        getClassBytes(untransformedName);
                        signers = entry.getCodeSigners();
                        if (pkg == null) {
                            pkg = definePackage(packageName, manifest, jarURLConnection.getJarFileURL());
                        } else {
                            if (pkg.isSealed() && !pkg.isSealed(jarURLConnection.getJarFileURL())) {
                                LogWrapper.severe("The jar file %s is trying to seal already secured path %s", jarFile.getName(), packageName);
                            } else if (isSealed(packageName, manifest)) {
                                LogWrapper.severe("The jar file %s has a security seal for path %s, but that path is defined and not secure", jarFile.getName(), packageName);
                            }
                        }
                    }
                } else {
                    Package pkg = getPackage(packageName);
                    if (pkg == null) {
                        pkg = definePackage(packageName, null, null, null, null, null, null, null);
                    } else if (pkg.isSealed()) {
                        LogWrapper.severe("The URL %s is defining elements for sealed path %s", urlConnection.getURL(), packageName);
                    }
                }
            }

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, getClassBytes(untransformedName));
            if (DEBUG_SAVE) {
                saveTransformedClass(transformedClass, transformedName);
            }

            String urlPath = URLDecoder.decode(urlConnection.getURL().getFile(), "UTF-8").split("!/")[0].substring(6);
            URL url = new File(urlPath).toURI().toURL();
            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(url, signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            this.cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            this.invalidClasses.add(name);
            if (DEBUG) {
                LogWrapper.log(Level.TRACE, e, "Exception encountered attempting classloading of %s", name);
                LogManager.getLogger("LaunchWrapper").log(Level.ERROR, "Exception encountered attempting classloading of %s", e);
            }
            throw new ClassNotFoundException(name, e);
        }
    }
    
    private static Object readStaticField(String fieldName) {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(LaunchClassLoader.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Object readField(String fieldName) {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Object invokeMethod(String methodName, Class<?>[] types, Object[] values) {
        try {
            Method method = LaunchClassLoader.class.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(this, values);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    private void saveTransformedClass(final byte[] data, final String transformedName) {
        this.invokeMethod("saveTransformedClass", new Class<?>[] { byte[].class, String.class }, new Object[] { data, transformedName });
    }
    
    private String untransformName(final String name) {
        return (String) this.invokeMethod("untransformName", new Class<?>[] { String.class }, new Object[] { name });
    }
    
    private String transformName(final String name) {
        return (String) this.invokeMethod("transformName", new Class<?>[] { String.class }, new Object[] { name });
    }
    
    private boolean isSealed(final String path, final Manifest manifest) {
        return (boolean) this.invokeMethod("isSealed", new Class<?>[] { String.class, Manifest.class }, new Object[] { path, manifest });
    }
    
    private URLConnection findCodeSourceConnectionFor(final String name) {
        return (URLConnection) this.invokeMethod("findCodeSourceConnectionFor", new Class<?>[] { String.class }, new Object[] { name });
    }
    
    private byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        return (byte[]) this.invokeMethod("runTransformers", new Class<?>[] { String.class, String.class, byte[].class  }, new Object[] { name, transformedName, basicClass });
    }
}
