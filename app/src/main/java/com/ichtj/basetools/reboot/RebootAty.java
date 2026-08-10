package com.ichtj.basetools.reboot;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.StartPageAty;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppManager;
import com.ichtj.basetools.util.PACKAGES;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

@Route(path = PACKAGES.BASE + "reboot")
public class RebootAty extends BaseActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 2101;
    private static final int REQUEST_OVERLAY_PERMISSION = 2102;
    private static final long UI_REFRESH_INTERVAL_MS = 1000L;
    private static final int[] CYCLE_PRESET_SECONDS = {
            30, 60, 300, 600, 900, 1800, 3600
    };
    private static final int[] CYCLE_PRESET_LABEL_IDS = {
            R.id.tvPreset30Seconds,
            R.id.tvPreset1Minute,
            R.id.tvPreset5Minutes,
            R.id.tvPreset10Minutes,
            R.id.tvPreset15Minutes,
            R.id.tvPreset30Minutes,
            R.id.tvPreset60Minutes
    };

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable statusTicker = new Runnable() {
        @Override
        public void run() {
            refreshTaskStatus();
            uiHandler.postDelayed(this, UI_REFRESH_INTERVAL_MS);
        }
    };

    private SwitchCompat switchRebootTask;
    private SwitchCompat switchBackgroundMode;
    private SwitchCompat switchOverlayStatus;
    private EditText etCycle;
    private SeekBar seekCyclePreset;
    private TextView tvCycleConversion;
    private TextView[] cyclePresetLabels;
    private TextView tvCountdown;
    private TextView tvTaskStatus;
    private TextView tvPauseReason;
    private TextView tvRebootCount;
    private TextView tvLastRebootTime;
    private TextView tvLogStats;
    private TextView tvLogPath;
    private View pausePanel;
    private Button btnUpdateCycle;
    private Button btnResumeTask;
    private RebootCustomService service;
    private boolean bindingRegistered;
    private boolean isBound;
    private boolean suppressSwitchCallback;
    private boolean suppressBackgroundSwitchCallback;
    private boolean suppressOverlaySwitchCallback;
    private boolean suppressCycleInputCallback;
    private boolean finishAfterOverlayPermission;
    private boolean backgroundChoicePending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot);
        bindViews();
        setupCycleSelector();
        setCycleInput(RebootStateStore.getCycleSeconds(this));
        setBackgroundSwitchChecked(RebootStateStore.isBackgroundModeEnabled(this));
        setOverlaySwitchChecked(RebootStateStore.isOverlayStatusEnabled(this));
        switchRebootTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!suppressSwitchCallback) {
                changeTaskEnabled(isChecked);
            }
        });
        switchBackgroundMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!suppressBackgroundSwitchCallback) {
                changeBackgroundMode(isChecked);
            }
        });
        switchOverlayStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!suppressOverlaySwitchCallback) {
                changeOverlayStatus(isChecked);
            }
        });
        refreshAllStatus();
        ensureStoragePermission();
        if (RebootStateStore.isEnabled(this)) {
            startAndBindService();
        }
        AppManager.finishActivity(StartPageAty.class);
    }

    private void bindViews() {
        switchRebootTask = findViewById(R.id.switchRebootTask);
        switchBackgroundMode = findViewById(R.id.switchBackgroundMode);
        switchOverlayStatus = findViewById(R.id.switchOverlayStatus);
        etCycle = findViewById(R.id.etCycle);
        seekCyclePreset = findViewById(R.id.seekCyclePreset);
        tvCycleConversion = findViewById(R.id.tvCycleConversion);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvPauseReason = findViewById(R.id.tvPauseReason);
        tvRebootCount = findViewById(R.id.tvRebootCount);
        tvLastRebootTime = findViewById(R.id.tvLastRebootTime);
        tvLogStats = findViewById(R.id.tvLogStats);
        tvLogPath = findViewById(R.id.tvLogPath);
        pausePanel = findViewById(R.id.pausePanel);
        btnUpdateCycle = findViewById(R.id.btnUpdateCycle);
        btnResumeTask = findViewById(R.id.btnResumeTask);
        cyclePresetLabels = new TextView[CYCLE_PRESET_LABEL_IDS.length];
        for (int index = 0; index < CYCLE_PRESET_LABEL_IDS.length; index++) {
            cyclePresetLabels[index] = findViewById(CYCLE_PRESET_LABEL_IDS[index]);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RebootCustomService.setSettingsActivityVisible(this, true);
    }

    @Override
    protected void onStop() {
        RebootCustomService.setSettingsActivityVisible(this, false);
        super.onStop();
    }

    private void setupCycleSelector() {
        seekCyclePreset.setMax(CYCLE_PRESET_SECONDS.length - 1);
        seekCyclePreset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    selectCyclePreset(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        for (int index = 0; index < cyclePresetLabels.length; index++) {
            final int presetIndex = index;
            cyclePresetLabels[index].setOnClickListener(
                    view -> selectCyclePreset(presetIndex));
        }
        etCycle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
                if (!suppressCycleInputCallback) {
                    syncCycleSelector(value == null ? "" : value.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable value) {
            }
        });
    }

    private void selectCyclePreset(int presetIndex) {
        if (presetIndex < 0 || presetIndex >= CYCLE_PRESET_SECONDS.length
                || !seekCyclePreset.isEnabled()) {
            return;
        }
        setCycleInput(CYCLE_PRESET_SECONDS[presetIndex]);
    }

    private void setCycleInput(int seconds) {
        suppressCycleInputCallback = true;
        etCycle.setText(String.valueOf(seconds));
        etCycle.setSelection(etCycle.length());
        suppressCycleInputCallback = false;
        syncCycleSelector(String.valueOf(seconds));
    }

    private void syncCycleSelector(String input) {
        final int seconds;
        try {
            seconds = Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            showInvalidCycleSelection();
            return;
        }
        if (seconds <= 0) {
            showInvalidCycleSelection();
            return;
        }

        int presetIndex = findCyclePresetIndex(seconds);
        updatePresetSelection(presetIndex);
        tvCycleConversion.setText(presetIndex >= 0
                ? getString(R.string.reboot_cycle_preset_conversion,
                formatCycleDuration(seconds), seconds)
                : getString(R.string.reboot_cycle_custom_conversion,
                seconds, formatCycleDuration(seconds)));
    }

    private void showInvalidCycleSelection() {
        updatePresetSelection(-1);
        tvCycleConversion.setText(R.string.reboot_cycle_conversion_hint);
    }

    private void updatePresetSelection(int selectedIndex) {
        if (selectedIndex >= 0 && seekCyclePreset.getProgress() != selectedIndex) {
            seekCyclePreset.setProgress(selectedIndex);
        }
        seekCyclePreset.setAlpha(selectedIndex >= 0 ? 1f : 0.55f);
        int selectedColor = ContextCompat.getColor(this, R.color.colorAccent);
        int normalColor = ContextCompat.getColor(this, R.color.reboot_text_secondary);
        for (int index = 0; index < cyclePresetLabels.length; index++) {
            boolean selected = index == selectedIndex;
            cyclePresetLabels[index].setSelected(selected);
            cyclePresetLabels[index].setTextColor(selected ? selectedColor : normalColor);
            cyclePresetLabels[index].setTypeface(null,
                    selected ? android.graphics.Typeface.BOLD
                            : android.graphics.Typeface.NORMAL);
        }
    }

    private static int findCyclePresetIndex(int seconds) {
        for (int index = 0; index < CYCLE_PRESET_SECONDS.length; index++) {
            if (CYCLE_PRESET_SECONDS[index] == seconds) {
                return index;
            }
        }
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RebootStateStore.isEnabled(this) && !bindingRegistered) {
            startAndBindService();
        }
        refreshAllStatus();
        uiHandler.removeCallbacks(statusTicker);
        uiHandler.post(statusTicker);
    }

    @Override
    protected void onPause() {
        uiHandler.removeCallbacks(statusTicker);
        super.onPause();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBound = true;
            service = ((RebootCustomService.TestBinder) binder).getService();
            refreshTaskStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            service = null;
            refreshTaskStatus();
        }
    };

    private void startAndBindService() {
        RebootCustomService.startServiceCompat(this);
        if (!bindingRegistered) {
            bindingRegistered = bindService(new Intent(this, RebootCustomService.class),
                    connection, BIND_AUTO_CREATE);
        }
    }

    private void unbindRebootService() {
        if (bindingRegistered) {
            unbindService(connection);
        }
        bindingRegistered = false;
        isBound = false;
        service = null;
    }

    private void changeTaskEnabled(boolean enabled) {
        boolean success;
        if (service != null) {
            success = service.setTaskEnabled(enabled);
        } else if (enabled) {
            success = RebootCustomService.enableTask(this);
        } else {
            success = RebootCustomService.disableTask(this);
        }

        if (!success) {
            setSwitchChecked(RebootStateStore.isEnabled(this));
            ToastUtils.error(getString(isRebootInProgress()
                    ? R.string.reboot_switch_locked : R.string.reboot_switch_update_failed));
            refreshTaskStatus();
            return;
        }

        if (enabled) {
            startAndBindService();
            ToastUtils.success(getString(R.string.reboot_task_enabled));
            if (RebootStateStore.isBackgroundModeEnabled(this)
                    && RebootStateStore.isOverlayStatusEnabled(this)) {
                requestOverlayPermissionIfNeeded();
            }
        } else {
            unbindRebootService();
            ToastUtils.info(getString(R.string.reboot_task_disabled));
        }
        refreshTaskStatus();
    }

    private void changeBackgroundMode(boolean enabled) {
        if (!enabled) {
            if (!RebootStateStore.setBackgroundModeEnabled(this, false)) {
                setBackgroundSwitchChecked(true);
                ToastUtils.error(getString(R.string.reboot_background_save_failed));
                return;
            }
            RebootCustomService.onBackgroundModeChanged(this, false, false);
            return;
        }

        backgroundChoicePending = true;
        new AlertDialog.Builder(this)
                .setTitle(R.string.reboot_background_dialog_title)
                .setMessage(R.string.reboot_background_dialog_message)
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> cancelBackgroundModeChoice())
                .setNeutralButton(R.string.reboot_background_next_boot,
                        (dialog, which) -> enableBackgroundMode(false))
                .setPositiveButton(R.string.reboot_background_finish_now,
                        (dialog, which) -> enableBackgroundMode(true))
                .setOnCancelListener(dialog -> cancelBackgroundModeChoice())
                .show();
    }

    private void cancelBackgroundModeChoice() {
        backgroundChoicePending = false;
        setBackgroundSwitchChecked(false);
    }

    private void enableBackgroundMode(boolean finishNow) {
        backgroundChoicePending = false;
        if (!RebootStateStore.setBackgroundModeEnabled(this, true)) {
            setBackgroundSwitchChecked(false);
            ToastUtils.error(getString(R.string.reboot_background_save_failed));
            return;
        }
        setBackgroundSwitchChecked(true);
        RebootCustomService.onBackgroundModeChanged(this, true, finishNow);
        boolean waitingForPermission = RebootStateStore.isEnabled(this)
                && RebootStateStore.isOverlayStatusEnabled(this)
                && requestOverlayPermissionIfNeeded();
        finishAfterOverlayPermission = finishNow && waitingForPermission;
        if (finishNow && !waitingForPermission) {
            finish();
        }
    }

    private void changeOverlayStatus(boolean enabled) {
        if (!RebootStateStore.setOverlayStatusEnabled(this, enabled)) {
            setOverlaySwitchChecked(!enabled);
            ToastUtils.error(getString(R.string.reboot_overlay_status_save_failed));
            return;
        }
        RebootCustomService.onOverlayStatusSettingChanged(this, enabled);
        if (enabled && RebootStateStore.isEnabled(this)
                && RebootStateStore.isBackgroundModeEnabled(this)) {
            requestOverlayPermissionIfNeeded();
        }
    }

    private boolean requestOverlayPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this)) {
            return false;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            RebootCustomService.onOverlayPermissionRequestStarted(this);
            return true;
        } catch (Throwable throwable) {
            RebootCustomService.onOverlayPermissionRequestUnavailable(this);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_OVERLAY_PERMISSION) {
            return;
        }
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        RebootCustomService.onOverlayPermissionResult(this, granted);
        if (finishAfterOverlayPermission) {
            finishAfterOverlayPermission = false;
            finish();
        }
    }

    public void setCycleClick(View view) {
        String input = etCycle.getText().toString().trim();
        if (input.length() == 0) {
            ToastUtils.error(getString(R.string.reboot_cycle_required));
            return;
        }

        final int seconds;
        try {
            seconds = Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            ToastUtils.error(getString(R.string.reboot_cycle_out_of_range));
            return;
        }
        if (seconds <= 0) {
            ToastUtils.error(getString(R.string.reboot_cycle_positive));
            return;
        }
        if (isRebootInProgress()) {
            ToastUtils.error(getString(R.string.reboot_controls_locked));
            return;
        }
        if (RebootStateStore.isEnabled(this) && service == null) {
            ToastUtils.error(getString(R.string.reboot_service_connecting));
            return;
        }

        boolean updated;
        if (service != null) {
            updated = service.updateCycleSeconds(seconds);
        } else {
            int bootCount = RebootStateStore.readBootCount(this);
            String marker = RebootStateStore.createBootMarker(
                    bootCount, RebootStateStore.readBootId());
            updated = RebootStateStore.updateCycleAndSchedule(
                    this, seconds, marker, SystemClock.elapsedRealtime());
        }
        if (updated) {
            int message = !RebootStateStore.isEnabled(this)
                    ? R.string.reboot_cycle_saved_disabled
                    : RebootStateStore.isPaused(this)
                    ? R.string.reboot_cycle_updated_paused : R.string.reboot_cycle_updated;
            ToastUtils.success(getString(message));
            refreshTaskStatus();
        } else {
            ToastUtils.error(getString(R.string.reboot_cycle_update_failed));
        }
    }

    public void resumeTaskClick(View view) {
        if (!isBound || service == null) {
            ToastUtils.error(getString(R.string.reboot_service_connecting));
            return;
        }
        if (service.resumeCycle()) {
            ToastUtils.success(getString(R.string.reboot_resume_success));
        } else {
            ToastUtils.error(getString(R.string.reboot_resume_failed));
        }
        refreshTaskStatus();
    }

    public void clearLogsClick(View view) {
        RebootLogStore.LogStats stats = RebootLogStore.getStats();
        if (stats.fileCount == 0) {
            ToastUtils.info(getString(R.string.reboot_no_logs));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.reboot_clear_title)
                .setMessage(getString(R.string.reboot_clear_message,
                        stats.fileCount, formatBytes(stats.totalBytes)))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.reboot_clear_action,
                        (dialog, which) -> clearLogsInBackground())
                .show();
    }

    private void clearLogsInBackground() {
        new Thread(() -> {
            RebootLogStore.ClearResult result = RebootLogStore.clearLogs();
            runOnUiThread(() -> {
                String message = getString(R.string.reboot_clear_result,
                        result.deletedFiles, result.failedFiles,
                        formatBytes(result.freedBytes));
                if (result.failedFiles == 0) {
                    ToastUtils.success(message);
                } else {
                    ToastUtils.error(message);
                }
                refreshLogStatus();
            });
        }, "reboot-log-cleaner").start();
    }

    private void refreshAllStatus() {
        refreshTaskStatus();
        refreshLogStatus();
    }

    private void refreshTaskStatus() {
        boolean enabled = RebootStateStore.isEnabled(this);
        boolean paused = enabled && (service != null
                ? service.isTaskPaused() : RebootStateStore.isPaused(this));
        boolean rebooting = enabled && isRebootInProgress();

        setSwitchChecked(enabled);
        if (!backgroundChoicePending) {
            setBackgroundSwitchChecked(RebootStateStore.isBackgroundModeEnabled(this));
        }
        setOverlaySwitchChecked(RebootStateStore.isOverlayStatusEnabled(this));
        switchRebootTask.setEnabled(!rebooting);
        etCycle.setEnabled(!rebooting);
        seekCyclePreset.setEnabled(!rebooting);
        for (TextView presetLabel : cyclePresetLabels) {
            presetLabel.setEnabled(!rebooting);
            presetLabel.setAlpha(rebooting ? 0.45f : 1f);
        }
        btnUpdateCycle.setEnabled(!rebooting && (!enabled || service != null));
        btnResumeTask.setVisibility(paused && !rebooting ? View.VISIBLE : View.GONE);
        btnResumeTask.setEnabled(service != null);
        tvPauseReason.setVisibility(paused && !rebooting ? View.VISIBLE : View.GONE);
        pausePanel.setVisibility(paused && !rebooting ? View.VISIBLE : View.GONE);

        if (!enabled) {
            tvTaskStatus.setText(R.string.reboot_task_status_disabled);
            tvCountdown.setText(R.string.reboot_countdown_disabled);
        } else if (rebooting) {
            tvTaskStatus.setText(R.string.reboot_task_status_executing);
            tvCountdown.setText(R.string.reboot_countdown_executing);
        } else if (paused) {
            tvTaskStatus.setText(R.string.reboot_task_status_paused);
            tvCountdown.setText(R.string.reboot_countdown_paused);
            String reason = service != null
                    ? service.getTaskPauseReason() : RebootStateStore.getPauseReason(this);
            tvPauseReason.setText(getString(R.string.reboot_pause_reason, reason));
        } else if (service == null) {
            tvTaskStatus.setText(R.string.reboot_task_status_starting);
            tvCountdown.setText(R.string.reboot_countdown_preparing);
        } else {
            long trigger = RebootStateStore.getNextTriggerElapsedMs(this);
            if (trigger <= 0L) {
                tvTaskStatus.setText(R.string.reboot_task_status_starting);
                tvCountdown.setText(R.string.reboot_countdown_preparing);
            } else {
                tvTaskStatus.setText(R.string.reboot_task_status_running);
                tvCountdown.setText(formatCountdown(
                        Math.max(0L, trigger - SystemClock.elapsedRealtime())));
            }
        }

        long count = RebootStateStore.getSuccessCount(this);
        tvRebootCount.setText(getString(R.string.reboot_success_count, count));
        long lastSuccess = RebootStateStore.getLastSuccessWallTime(this);
        tvLastRebootTime.setText(lastSuccess > 0L
                ? getString(R.string.reboot_last_time,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
                        .format(new Date(lastSuccess)))
                : getString(R.string.reboot_last_time_none));
    }

    private boolean isRebootInProgress() {
        return service != null
                ? service.isRebootInProgress()
                : RebootStateStore.getPendingTask(this) != null;
    }

    private void setSwitchChecked(boolean checked) {
        if (switchRebootTask.isChecked() == checked) {
            return;
        }
        suppressSwitchCallback = true;
        switchRebootTask.setChecked(checked);
        suppressSwitchCallback = false;
    }

    private void setBackgroundSwitchChecked(boolean checked) {
        if (switchBackgroundMode.isChecked() == checked) {
            return;
        }
        suppressBackgroundSwitchCallback = true;
        switchBackgroundMode.setChecked(checked);
        suppressBackgroundSwitchCallback = false;
    }

    private void setOverlaySwitchChecked(boolean checked) {
        if (switchOverlayStatus.isChecked() == checked) {
            return;
        }
        suppressOverlaySwitchCallback = true;
        switchOverlayStatus.setChecked(checked);
        suppressOverlaySwitchCallback = false;
    }

    private void refreshLogStatus() {
        RebootLogStore.LogStats stats = RebootLogStore.getStats();
        tvLogStats.setText(getString(R.string.reboot_log_summary,
                stats.fileCount, formatBytes(stats.totalBytes)));
        tvLogPath.setText(getString(R.string.reboot_log_path,
                RebootLogStore.getLogDirectory().getAbsolutePath()));
    }

    private void ensureStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION
                && (grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            ToastUtils.error(getString(R.string.reboot_storage_denied));
        }
    }

    private static String formatCountdown(long durationMs) {
        long totalSeconds = Math.max(0L, (durationMs + 999L) / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatCycleDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes == 0) {
            return getString(R.string.reboot_duration_seconds, seconds);
        }
        if (seconds == 0) {
            return getString(R.string.reboot_duration_minutes, minutes);
        }
        return getString(R.string.reboot_duration_minutes_seconds, minutes, seconds);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        if (bytes < 1024L * 1024L) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024d);
        }
        return String.format(Locale.US, "%.1f MB", bytes / (1024d * 1024d));
    }

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacks(statusTicker);
        unbindRebootService();
        super.onDestroy();
    }
}
