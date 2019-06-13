import java.util.List;

import com.google.common.collect.Lists;
import io.github.zekerzhayard.noserverargument.launch.NewMinecraftLauncher;
import net.minecraftforge.common.ForgeVersion;
import org.apache.commons.lang3.reflect.FieldUtils;

public class NoServerArgumentHook {
    public static String[] removeServerArgs(String[] args) throws IllegalAccessException {
        String minecraftVersion = "1.12.2";
        List<String> argsList = Lists.newArrayList(args);
        System.out.println(argsList);
        for (int i = argsList.size() - 1; i >= 0; i--) {
            if (argsList.get(i).equals(FieldUtils.readStaticField(ForgeVersion.class, "mcVersion"))) {
                System.out.println("Remove: " + argsList.remove(i));
                argsList.add(i, minecraftVersion);
            } else if (argsList.get(i).equals("--server")) {
                System.out.println("Remove: " + argsList.remove(i));
                System.out.println("Remove: " + argsList.remove(i));
            } else if (argsList.get(i).equals("--port")) {
                System.out.println("Remove: " + argsList.remove(i));
                System.out.println("Remove: " + argsList.remove(i));
            }
        }
        args = argsList.toArray(new String[0]);
        System.out.println(argsList);
        new NewMinecraftLauncher(minecraftVersion, args).start();
        return args;
    }
}
