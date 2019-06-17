package io.github.zekerzhayard.noserverargument.asm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class LogFileUtil {
    public static void gzipLatestLog(String logDir) {
        File latestLog = new File(logDir, "latest.log");
        if (!latestLog.exists()) {
            return;
        }
        String logDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(latestLog.lastModified()));
        for (int i = 1; i <= 7; i++) {
            File currentLogGz = new File(logDir, logDate + "-" + i + ".log.gz");
            if (!currentLogGz.exists()) {
                LogFileUtil.gzipLogFile(latestLog.getAbsolutePath(), currentLogGz.getAbsolutePath());
                break;
            }
        }
    }
    
    public static void gzipLogFile(String source, String target) {
        try (
                GZIPOutputStream logGzip = new GZIPOutputStream(new FileOutputStream(target));
                FileInputStream logFile = new FileInputStream(source);
        ) {
            byte fileByte[] = new byte[(int) new File(source).length()];
            int length = 0;
            while ((length = logFile.read(fileByte)) != -1) {
                logGzip.write(fileByte, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
