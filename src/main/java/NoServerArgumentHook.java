import org.apache.commons.lang3.ArrayUtils;

public class NoServerArgumentHook {
    public static String[] removeServerArgs(String[] args) {
        System.out.println(args);
        int i = ArrayUtils.indexOf(args, "--server");
        if (i != -1) {
            args = ArrayUtils.removeAll(args, i, i + 1);
        }
        int j = ArrayUtils.indexOf(args, "--port");
        if (j != -1) {
            args = ArrayUtils.removeAll(args, j, j + 1);
        }
        return args;
    }
}
