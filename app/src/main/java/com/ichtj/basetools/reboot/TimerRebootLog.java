package com.ichtj.basetools.reboot;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.TimeZone;

/** One atomic JSON document for the latest attempt, never an appended event stream. */
final class TimerRebootLog {
    private static final String TAG = "ichtj_timer_reboot";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private TimerRebootLog() {
    }

    static File file(Context context) {
        return new File(context.getFilesDir(), "ichtj_timer_reboot_latest.json");
    }

    static synchronized void start(Context context, String taskId, TimerRebootPlan plan,
                                   long scheduledTime, long requestTime, String bootId,
                                   int bootCount) throws Exception {
        JSONObject record = new JSONObject();
        record.put("taskId", taskId);
        record.put("type", plan.type);
        record.put("hour", plan.hour);
        record.put("minute", plan.minute);
        record.put("year", plan.year);
        record.put("month", plan.month);
        record.put("day", plan.day);
        record.put("weekdays", plan.weekdays);
        record.put("monthDay", plan.monthDay);
        record.put("timeZone", TimeZone.getDefault().getID());
        record.put("scheduledTimeMs", scheduledTime);
        record.put("requestTimeMs", requestTime);
        record.put("requestBootId", bootId);
        record.put("requestBootCount", bootCount);
        record.put("result", "REQUESTED");
        record.put("detail", "");
        write(context, record);
    }

    static synchronized void finish(Context context, String taskId, String result,
                                    String detail) throws Exception {
        JSONObject record = read(context);
        if (!taskId.equals(record.optString("taskId"))) {
            throw new IllegalStateException("Latest log task does not match pending reboot");
        }
        record.put("result", result);
        record.put("detail", detail == null ? "" : detail);
        record.put("resultTimeMs", System.currentTimeMillis());
        write(context, record);
    }

    static synchronized JSONObject read(Context context) throws Exception {
        return new JSONObject(new String(new AtomicFile(file(context)).readFully(), UTF_8));
    }

    private static void write(Context context, JSONObject record) throws Exception {
        AtomicFile atomic = new AtomicFile(file(context));
        FileOutputStream stream = null;
        try {
            byte[] bytes = (record.toString(2) + "\n").getBytes(UTF_8);
            stream = atomic.startWrite();
            stream.write(bytes);
            stream.flush();
            stream.getFD().sync();
            atomic.finishWrite(stream);
            stream = null;
            if (!Arrays.equals(bytes, atomic.readFully())) {
                throw new java.io.IOException("Latest reboot log verification failed");
            }
        } catch (Exception error) {
            if (stream != null) {
                atomic.failWrite(stream);
            }
            Log.e(TAG, "Latest reboot log could not be saved", error);
            throw error;
        }
    }
}
