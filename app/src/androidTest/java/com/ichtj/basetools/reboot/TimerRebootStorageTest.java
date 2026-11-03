package com.ichtj.basetools.reboot;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ichtj.basetools.util.BaseIotReceiver;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Isolated storage checks: these tests never schedule alarms or request a reboot. */
@RunWith(AndroidJUnit4.class)
public class TimerRebootStorageTest {
    private Context context;
    private File directory;

    @Before
    public void setUp() {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String suffix = "ichtj_timer_test_" + System.nanoTime();
        directory = new File(target.getCacheDir(), suffix);
        assertTrue(directory.mkdirs());
        context = new ContextWrapper(target) {
            @Override
            public File getFilesDir() {
                return directory;
            }

            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences(suffix, mode);
            }

            @Override
            public Object getSystemService(String name) {
                // Never touch production alarms, even when checking scheduler failure paths.
                return Context.ALARM_SERVICE.equals(name) ? null : super.getSystemService(name);
            }
        };
    }

    @After
    public void tearDown() {
        TimerRebootStore.prefs(context).edit().clear().commit();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                assertTrue(file.delete());
            }
        }
        assertTrue(directory.delete());
    }

    @Test
    public void firstUseDefaultsToDailyAtTwoAndEnabled() {
        assertTrue(TimerRebootStore.enabled(context));
        assertTrue(TimerRebootStore.configured(context));
        assertEquals(TimerRebootPlan.DAILY, TimerRebootStore.plan(context).type);
        assertEquals(2, TimerRebootStore.plan(context).hour);
        assertEquals(0, TimerRebootStore.plan(context).minute);
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertTrue(TimerRebootStore.prefs(context).getBoolean("enabled", false));
        assertEquals(2, TimerRebootStore.prefs(context).getInt("hour", -1));
        assertFalse(TimerRebootStore.generation(context).isEmpty());
    }

    @Test
    public void savingADisabledPlanDoesNotReenableIt() {
        assertTrue(TimerRebootStore.setEnabled(context, false));
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.configured(context));
        assertFalse(TimerRebootStore.enabled(context));
        assertEquals(18, TimerRebootStore.plan(context).hour);
    }

    @Test
    public void newRequestOverwritesOldLogRatherThanAppending() throws Exception {
        TimerRebootLog.start(context, "task-old", plan(18), 1000L, 1001L, "boot-a", 1);
        TimerRebootLog.finish(context, "task-old", "SUCCESS", "confirmed");
        TimerRebootLog.start(context, "task-new", plan(19), 2000L, 2001L, "boot-b", 2);
        JSONObject latest = TimerRebootLog.read(context);
        assertEquals("task-new", latest.getString("taskId"));
        assertEquals("REQUESTED", latest.getString("result"));
        assertFalse(latest.toString().contains("task-old"));
        assertFalse(latest.has("resultTimeMs"));
        assertEquals(1, directory.listFiles().length);
    }

    @Test
    public void bootResultUpdatesSameRecordWithoutAddingFiles() throws Exception {
        TimerRebootLog.start(context, "task-1", plan(18), 1000L, 1001L, "boot-a", 1);
        TimerRebootLog.finish(context, "task-1", "SUCCESS", "boot confirmed");
        JSONObject record = TimerRebootLog.read(context);
        assertEquals("task-1", record.getString("taskId"));
        assertEquals(1000L, record.getLong("scheduledTimeMs"));
        assertEquals("SUCCESS", record.getString("result"));
        assertEquals(1, directory.listFiles().length);
    }

    @Test
    public void unrelatedTaskCannotOverwriteLatestResult() throws Exception {
        TimerRebootLog.start(context, "task-1", plan(18), 1000L, 1001L, "boot-a", 1);
        try {
            TimerRebootLog.finish(context, "task-other", "SUCCESS", "wrong task");
            fail("Task mismatch must be rejected");
        } catch (IllegalStateException expected) {
            assertEquals("REQUESTED", TimerRebootLog.read(context).getString("result"));
        }
    }

    @Test
    public void logWriteFailureIsReportedToCaller() throws Exception {
        File blocker = new File(directory, "not-a-directory");
        assertTrue(blocker.createNewFile());
        Context blocked = new ContextWrapper(context) {
            @Override
            public File getFilesDir() {
                return blocker;
            }
        };
        try {
            TimerRebootLog.start(blocked, "task-1", plan(18), 1000L, 1001L, "boot-a", 1);
            fail("A file cannot be used as the log parent directory");
        } catch (java.io.IOException expected) {
            assertFalse(TimerRebootLog.file(blocked).exists());
        }
    }

    @Test
    public void savingWhilePausedDoesNotResumeThePlan() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.setEnabled(context, true));
        assertTrue(TimerRebootStore.pause(context, TimerRebootStore.ERROR_REBOOT, "test"));
        assertTrue(TimerRebootStore.save(context, plan(19)));
        assertTrue(TimerRebootStore.paused(context));
        assertTrue(TimerRebootStore.enabled(context));
    }

    @Test
    public void disablingAndSavingKeepConfigurationWithoutReenablingIt() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.setEnabled(context, true));
        assertTrue(TimerRebootStore.setEnabled(context, false));
        assertTrue(TimerRebootStore.save(context, plan(19)));
        assertFalse(TimerRebootStore.enabled(context));
        assertEquals(19, TimerRebootStore.plan(context).hour);
    }

    @Test
    public void deduplicationIsPreservedOnlyForTheSameSchedule() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.begin(context, "test", 1000L, 100L,
                "boot-a", 1, "2026-11-02"));
        assertTrue(TimerRebootStore.finish(context, "SUCCESS", 2000L, 0, ""));
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertEquals("2026-11-02", TimerRebootStore.lastOccurrence(context));
        assertTrue(TimerRebootStore.save(context, plan(19)));
        assertEquals("", TimerRebootStore.lastOccurrence(context));
    }

    @Test
    public void logFailurePausesExecutionBeforeARebootRequestIsPersisted() throws Exception {
        File blocker = new File(directory, "not-a-directory");
        assertTrue(blocker.createNewFile());
        Context blocked = new ContextWrapper(context) {
            @Override
            public File getFilesDir() {
                return blocker;
            }
        };
        assertTrue(TimerRebootStore.save(blocked, plan(18)));
        assertTrue(TimerRebootStore.setEnabled(blocked, true));
        long now = System.currentTimeMillis();
        assertTrue(TimerRebootStore.schedule(blocked, now, now, SystemClock.elapsedRealtime()));
        Intent fire = new Intent(TimerRebootScheduler.ACTION_FIRE)
                .putExtra("generation", TimerRebootStore.generation(blocked))
                .putExtra("trigger", now);

        TimerRebootScheduler.fire(blocked, fire);

        assertTrue(TimerRebootStore.paused(blocked));
        assertFalse(TimerRebootStore.pending(blocked));
        assertEquals(TimerRebootStore.ERROR_STORAGE,
                TimerRebootStore.prefs(blocked).getInt("error", 0));
        assertEquals(0L, TimerRebootStore.next(blocked));
    }

    @Test
    public void staleAlarmCannotWriteLogsOrRequestReboot() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.setEnabled(context, true));
        long now = System.currentTimeMillis();
        assertTrue(TimerRebootStore.schedule(context, now, now, SystemClock.elapsedRealtime()));
        TimerRebootScheduler.fire(context, new Intent(TimerRebootScheduler.ACTION_FIRE)
                .putExtra("generation", "old-generation").putExtra("trigger", now));
        assertFalse(TimerRebootStore.pending(context));
        assertFalse(TimerRebootLog.file(context).exists());
    }

    @Test
    public void defaultInitializationPreservesUserDisabledPlan() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.setEnabled(context, false));
        String generation = TimerRebootStore.generation(context);
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertFalse(TimerRebootStore.enabled(context));
        assertEquals(18, TimerRebootStore.plan(context).hour);
        assertEquals(generation, TimerRebootStore.generation(context));
    }

    @Test
    public void defaultInitializationPreservesPartialDisabledState() {
        assertTrue(TimerRebootStore.prefs(context).edit().putBoolean("enabled", false).commit());
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertFalse(TimerRebootStore.enabled(context));
        assertEquals(2, TimerRebootStore.plan(context).hour);
        assertTrue(TimerRebootStore.configured(context));
    }

    @Test
    public void defaultInitializationDoesNotClearPausedOrPendingRequests() {
        assertTrue(TimerRebootStore.save(context, plan(18)));
        assertTrue(TimerRebootStore.begin(context, "test", 1000L, 100L,
                "boot-a", 1, "2026-11-02"));
        assertTrue(TimerRebootStore.pause(context, TimerRebootStore.ERROR_STORAGE, "test"));
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertTrue(TimerRebootStore.paused(context));
        assertTrue(TimerRebootStore.pending(context));
        assertEquals("test", TimerRebootStore.prefs(context).getString("pendingId", ""));
    }

    @Test
    public void backgroundRestoreInitializesDefaults() {
        TimerRebootScheduler.restore(context);
        assertTrue(TimerRebootStore.configured(context));
        assertTrue(TimerRebootStore.enabled(context));
        assertEquals(TimerRebootPlan.DAILY, TimerRebootStore.plan(context).type);
        assertEquals(2, TimerRebootStore.plan(context).hour);
        // This isolated context deliberately has no AlarmManager.
        assertEquals(TimerRebootStore.ERROR_ALARM,
                TimerRebootStore.prefs(context).getInt("error", 0));
    }

    @Test
    public void bootStartsRestoreServiceWithoutOpeningAnActivity() {
        assertTrue(TimerRebootStore.initializeDefaults(context));
        Intent service = captureBootService();
        assertEquals(TimerRebootService.class.getName(), service.getComponent().getClassName());
        assertEquals(TimerRebootScheduler.ACTION_RESTORE, service.getAction());
    }

    @Test
    public void bootServiceDispatchDoesNotReenableAUserDisabledPlan() {
        assertTrue(TimerRebootStore.initializeDefaults(context));
        assertTrue(TimerRebootStore.setEnabled(context, false));
        assertEquals(TimerRebootScheduler.ACTION_RESTORE, captureBootService().getAction());
        assertFalse(TimerRebootStore.enabled(context));
    }

    @Test
    public void packageReplacementStartsRestoreServiceWithoutOpeningAnActivity() {
        Intent service = captureService(new Intent(Intent.ACTION_MY_PACKAGE_REPLACED));
        assertTrue(service != null);
        assertEquals(TimerRebootScheduler.ACTION_RESTORE, service.getAction());
    }

    @Test
    public void timeChangeStartsBackgroundReschedulingWithoutOpeningAnActivity() {
        Intent service = captureService(new Intent(Intent.ACTION_TIME_CHANGED));
        assertTrue(service != null);
        assertEquals(Intent.ACTION_TIME_CHANGED, service.getAction());
    }

    @Test
    public void timezoneChangeStartsBackgroundReschedulingWithoutOpeningAnActivity() {
        Intent service = captureService(new Intent(Intent.ACTION_TIMEZONE_CHANGED));
        assertTrue(service != null);
        assertEquals(Intent.ACTION_TIMEZONE_CHANGED, service.getAction());
    }

    @Test
    public void rebootAlarmKeepsItsGenerationAndTriggerWhenForwardedToService() {
        Intent service = captureService(new Intent(TimerRebootScheduler.ACTION_FIRE)
                .putExtra("generation", "test-generation").putExtra("trigger", 123456L));
        assertTrue(service != null);
        assertEquals(TimerRebootScheduler.ACTION_FIRE, service.getAction());
        assertEquals("test-generation", service.getStringExtra("generation"));
        assertEquals(123456L, service.getLongExtra("trigger", -1L));
    }

    @Test
    public void confirmationAlarmStartsBackgroundRestoreWithoutOpeningAnActivity() {
        Intent service = captureService(new Intent(TimerRebootScheduler.ACTION_CONFIRM));
        assertTrue(service != null);
        assertEquals(TimerRebootScheduler.ACTION_CONFIRM, service.getAction());
    }

    @Test
    public void unknownOrNullBroadcastDoesNotStartAServiceOrOpenAnActivity() {
        assertTrue(captureService(new Intent("ichtj_test_unknown_action")) == null);
        assertTrue(captureService(null) == null);
    }

    @Test
    public void unavailableAlarmManagerPausesInsteadOfClaimingAnActiveSchedule() {
        assertTrue(TimerRebootStore.setEnabled(context, false));
        assertTrue(TimerRebootScheduler.save(context, plan(18)));
        assertFalse(TimerRebootScheduler.enable(context, true));
        assertTrue(TimerRebootStore.paused(context));
        assertEquals(TimerRebootStore.ERROR_ALARM,
                TimerRebootStore.prefs(context).getInt("error", 0));
    }

    private Intent captureBootService() {
        Intent service = captureService(new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertTrue(service != null);
        return service;
    }

    private Intent captureService(Intent broadcast) {
        Intent[] started = new Intent[1];
        Context receiverContext = new ContextWrapper(context) {
            @Override
            public ComponentName startService(Intent service) {
                started[0] = service;
                return service.getComponent();
            }

            @Override
            public ComponentName startForegroundService(Intent service) {
                started[0] = service;
                return service.getComponent();
            }

            @Override
            public void startActivity(Intent intent) {
                fail("Timer broadcasts must not open an Activity");
            }
        };
        new BaseIotReceiver().onReceive(receiverContext, broadcast);
        return started[0];
    }

    private static TimerRebootPlan plan(int hour) {
        return new TimerRebootPlan(TimerRebootPlan.DAILY, hour, 0, 2026, 11, 2, 2, 1);
    }
}
