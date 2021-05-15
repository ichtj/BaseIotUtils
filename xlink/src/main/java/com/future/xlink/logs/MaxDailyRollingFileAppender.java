package com.future.xlink.logs;


import com.future.xlink.utils.GlobalConfig;

import org.apache.log4j.Layout;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MaxDailyRollingFileAppender extends IDailyRollingFileAppender {
    private int maxFileSize;

    public MaxDailyRollingFileAppender(Layout layout, String filename, String datePattern, int maxFileSize) throws IOException {
        super(layout, filename, datePattern);
        this.maxFileSize = maxFileSize;
    }

    void rollOver() throws IOException {
        super.rollOver();
        deleteOvermuch();
    }

    private void deleteOvermuch() {
        File logPath = new File(fileName).getParentFile();
        File[] files = logPath.listFiles((dir, name) -> name.contains(GlobalConfig.PATH_LOG_SUFFIX + "."));

        Arrays.sort(files, (o1, o2) -> o2.getName().compareTo(o1.getName()));

        if (files.length < maxFileSize) return;

        for (int i = maxFileSize - 1; i < files.length; i++) {
            files[i].delete();
        }
    }
}