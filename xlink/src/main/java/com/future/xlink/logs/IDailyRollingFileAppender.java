package com.future.xlink.logs;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author lee
 */
public class IDailyRollingFileAppender extends FileAppender {

    /**
     * The date pattern. By default, the pattern is set to
     * "'.'yyyy-MM-dd" meaning daily rollover.
     */
    private String datePattern = "'.'yyyy-MM-dd";

    /**
     * The log file will be renamed to the value of the
     * scheduledFilename variable when the next interval is entered. For
     * example, if the rollover period is one hour, the log file will be
     * renamed to the value of "scheduledFilename" at the beginning of
     * the next hour.
     * <p>
     * The precise time when a rollover occurs depends on logging
     * activity.
     */
    private String scheduledFilename;

    /**
     * The next time we estimate a rollover should occur.
     */
    private long nextCheck = System.currentTimeMillis() - 1;

    Date now = new Date();

    SimpleDateFormat sdf;

    RollingCalendar rc = new RollingCalendar();

    int checkPeriod = RollingCalendar.TOP_OF_TROUBLE;

    static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");


    /**
     * The default constructor does nothing.
     */
    public IDailyRollingFileAppender() {
    }

    /**
     * Instantiate a <code>DailyRollingFileAppender</code> and open the
     * file designated by <code>filename</code>. The opened filename will
     * become the ouput destination for this appender.
     */
    public IDailyRollingFileAppender(Layout layout, String filename,
                                     String datePattern) throws IOException {
        super(layout, filename, true);
        this.datePattern = datePattern;
        activateOptions();
    }

    /**
     * The <b>DatePattern</b> takes a string in the same format as
     * expected by {@link SimpleDateFormat}. This options determines the
     * rollover schedule.
     */
    public void setDatePattern(String pattern) {
        datePattern = pattern;
    }

    /**
     * Returns the value of the <b>DatePattern</b> option.
     */
    public String getDatePattern() {
        return datePattern;
    }

    public void activateOptions() {
        super.activateOptions();
        if (datePattern != null && fileName != null) {
            now.setTime(System.currentTimeMillis());
            sdf = new SimpleDateFormat(datePattern, Locale.getDefault());
            int type = computeCheckPeriod();
            printPeriodicity(type);
            rc.setType(type);
            File file = new File(fileName);
            scheduledFilename = fileName + sdf.format(new Date(file.lastModified()));

        } else {
            LogLog.error("Either File or DatePattern options are not set for appender ["
                    + name + "].");
        }
    }

    void printPeriodicity(int type) {
        switch (type) {
            case RollingCalendar.TOP_OF_MINUTE:
                LogLog.debug("Appender [" + name + "] to be rolled every minute.");
                break;
            case RollingCalendar.TOP_OF_HOUR:
                LogLog.debug("Appender [" + name
                        + "] to be rolled on top of every hour.");
                break;
            case RollingCalendar.HALF_DAY:
                LogLog.debug("Appender [" + name
                        + "] to be rolled at midday and midnight.");
                break;
            case RollingCalendar.TOP_OF_DAY:
                LogLog.debug("Appender [" + name
                        + "] to be rolled at midnight.");
                break;
            case RollingCalendar.TOP_OF_WEEK:
                LogLog.debug("Appender [" + name
                        + "] to be rolled at start of week.");
                break;
            case RollingCalendar.TOP_OF_MONTH:
                LogLog.debug("Appender [" + name
                        + "] to be rolled at start of every month.");
                break;
            default:
                LogLog.warn("Unknown periodicity for appender [" + name + "].");
        }
    }


    // This method computes the roll over period by looping over the
    // periods, starting with the shortest, and stopping when the r0 is
    // different from from r1, where r0 is the epoch formatted according
    // the datePattern (supplied by the user) and r1 is the
    // epoch+nextMillis(i) formatted according to datePattern. All date
    // formatting is done in GMT and not local format because the test
    // logic is based on comparisons relative to 1970-01-01 00:00:00
    // GMT (the epoch).

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
        // set sate to 1970-01-01 00:00:00 GMT
        Date epoch = new Date(0);
        if (datePattern != null) {
            for (int i = RollingCalendar.TOP_OF_MINUTE; i <= RollingCalendar.TOP_OF_MONTH; i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1)) {
                    return i;
                }
            }
        }
        return RollingCalendar.TOP_OF_TROUBLE;
    }

    /**
     * Rollover the current file to a new file.
     */
    void rollOver() throws IOException {

        /* Compute filename, but only if datePattern is specified */
        if (datePattern == null) {
            errorHandler.error("Missing DatePattern option in rollOver().");
            return;
        }

        String datedFilename = fileName + sdf.format(now);
        // It is too early to roll over because we are still within the
        // bounds of the current interval. Rollover will occur once the
        // next interval is reached.
        if (scheduledFilename.equals(datedFilename)) {
            return;
        }

        // close current file, and rename it to datedFilename
        this.closeFile();

        File target = new File(scheduledFilename);
        if (target.exists()) {
            target.delete();
        }

        File file = new File(fileName);
        boolean result = file.renameTo(target);
        if (result) {
            LogLog.debug(fileName + " -> " + scheduledFilename);
        } else {
            LogLog.error("Failed to rename [" + fileName + "] to [" + scheduledFilename + "].");
        }

        try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            this.setFile(fileName, true, this.bufferedIO, this.bufferSize);
        } catch (IOException e) {
            errorHandler.error("setFile(" + fileName + ", true) call failed.");
        }
        scheduledFilename = datedFilename;
    }

    /**
     * This method differentiates DailyRollingFileAppender from its
     * super class.
     * <p>
     * <p>Before actually logging, this method will check whether it is
     * time to do a rollover. If it is, it will schedule the next
     * rollover time and then rollover.
     */
    protected void subAppend(LoggingEvent event) {
        long n = System.currentTimeMillis();
        if (n >= nextCheck) {
            now.setTime(n);
            nextCheck = rc.getNextCheckMillis(now);
            try {
                rollOver();
            } catch (IOException ioe) {
                if (ioe instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("rollOver() failed.", ioe);
            }
        }
        super.subAppend(event);
    }
}

