package com.future.xlink.logs;

import android.util.Log;


import androidx.annotation.NonNull;

import com.future.xlink.utils.GlobalConfig;
import com.future.xlink.utils.JckJsonHelper;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * 日志工具配置
 *
 * @author lee
 */
public class Log4J {
    private static final String PATTERN_FILE = "[%-d{mm:ss}][%t][%p] -%l %m%n";
    private static final String FILE_SUFFIX = "'.'yyyy-MM-dd-HH";
    private static final String FILE_ENCODE = "UTF-8";
    /**
     * 保存10天数据
     */
    private static final int MAX_COUNT = 240;
    private static Logger errorLogger;
    private static Logger infoLogger;

    static {
        errorLogger = Logger.getLogger("Error");
        infoLogger = Logger.getLogger("Info");

        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("Debug", Level.INFO);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.setUseFileAppender(false);
        logConfigurator.setResetConfiguration(true);
        logConfigurator.setInternalDebugging(false);
        logConfigurator.configure();

        try {
            MaxDailyRollingFileAppender dailyAppender = new MaxDailyRollingFileAppender(
                    new PatternLayout(PATTERN_FILE), GlobalConfig.PATH_LOG_ERROR, FILE_SUFFIX, MAX_COUNT);
            dailyAppender.setImmediateFlush(true);
            dailyAppender.setAppend(true);
            dailyAppender.setThreshold(Level.ERROR);
            dailyAppender.setEncoding(FILE_ENCODE);
            errorLogger.addAppender(dailyAppender);

            MaxDailyRollingFileAppender _dailyAppender = new MaxDailyRollingFileAppender(
                    new PatternLayout(PATTERN_FILE), GlobalConfig.PATH_LOG_INFO, FILE_SUFFIX, MAX_COUNT);
            _dailyAppender.setImmediateFlush(true);
            _dailyAppender.setAppend(true);
            _dailyAppender.setThreshold(Level.INFO);
            _dailyAppender.setEncoding(FILE_ENCODE);
            infoLogger.addAppender(_dailyAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String concat(Class <?> clazz, String method) {
        return clazz.getName() + "#" + method;
    }

    private static String concat(Class <?> clazz, String method, String value) {
        return clazz.getName() + "#" + method + ":" + value;
    }

    public static void crash(Class <?> clazz, String method, @NonNull Throwable e) {
        errorLogger.error(concat(clazz, method), e);
    }

    public static void info(Class <?> clazz, String method, @NonNull Throwable e) {
        infoLogger.info(concat(clazz, method), e);
    }

    public static void info(Class <?> clazz, String method, @NonNull String value) {
        infoLogger.info(concat(clazz, method, value));
    }

    public static void json(Class <?> clazz, String method, @NonNull Object value) {
        infoLogger.info(concat(clazz, method, JckJsonHelper.toJson(value)));
    }

    public static void http(String msg) {
        Log.d("Http", msg);
    }
}