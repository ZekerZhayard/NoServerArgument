import java.lang.reflect.Method;

public class AuthLibHook {
    private static ClassLoader classLoader = AuthLibHook.class.getClassLoader().getClass().getClassLoader().getClass().getClassLoader();
    
    public static Boolean loadLibrary() throws Exception {
        Class<?> authenticationCpp = AuthLibHook.classLoader.loadClass("com.mojang.authlib.AuthenticationCpp");
        Method loadLibrary = authenticationCpp.getMethod("LoadLibrary");
        return (Boolean) loadLibrary.invoke(authenticationCpp);
    }
    
    public static Boolean authentication(int port, String serverId) throws Exception {
        Class<?> authenticationCpp = AuthLibHook.classLoader.loadClass("com.mojang.authlib.AuthenticationCpp");
        Method authentication = authenticationCpp.getMethod("Authentication", int.class, String.class);
        return (Boolean) authentication.invoke(authenticationCpp.newInstance(), port, serverId);
    }
}
