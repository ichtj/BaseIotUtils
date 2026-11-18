package com.ichtj.basetools.reboot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimerRebootAty extends BaseActivity
        implements TimerRebootTimeDialog.OnTimeSelectedListener {
    private static final String TAG=TimerRebootAty.class.getSimpleName();
    private static final int[] TYPE_IDS = {
            R.id.timerOnce, R.id.timerDaily, R.id.timerWeekly, R.id.timerMonthly
    };
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshStatus();
            handler.postDelayed(this, 1000L);
        }
    };
    private final CheckBox[] weekdays = new CheckBox[7];
    private SwitchCompat enabled;
    private RadioGroup types;
    private Spinner monthDay;
    private Button date;
    private Button time;
    private Button save;
    private View errorPanel;
    private TextView status;
    private TextView countdown;
    private TextView next;
    private TextView activeRule;
    private TextView error;
    private TextView lastResult;
    private View dirtyLabel;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private boolean suppress;
    private boolean dirty;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_reboot);
        enabled = findViewById(R.id.timerEnabled);
        types = findViewById(R.id.timerTypes);
        monthDay = findViewById(R.id.timerMonthDay);
        date = findViewById(R.id.timerDate);
        time = findViewById(R.id.timerTime);
        save = findViewById(R.id.timerSave);
        status = findViewById(R.id.timerStatus);
        countdown = findViewById(R.id.timerCountdown);
        next = findViewById(R.id.timerNext);
        activeRule = findViewById(R.id.timerActiveRule);
        errorPanel = findViewById(R.id.timerErrorPanel);
        error = findViewById(R.id.timerError);
        lastResult = findViewById(R.id.timerLastResult);
        dirtyLabel = findViewById(R.id.timerDirty);
        ((TextView) findViewById(R.id.timerLogPath)).setText(TimerRebootLog.file(this).getAbsolutePath());
        setupSelectors();
        loadDraft(savedInstanceState);
        enabled.setOnCheckedChangeListener((button, checked) -> {
            if (suppress) {
                return;
            }
            if (checked && (dirty || !TimerRebootStore.configured(this))) {
                ToastUtils.error(getString(R.string.timer_reboot_save_first));
                refreshStatus();
                return;
            }
            changeEnabled(checked);
        });
        save.setOnClickListener(view -> saveDraft());
        findViewById(R.id.timerResume).setOnClickListener(view -> changeEnabled(true));
        findViewById(R.id.timerViewLog).setOnClickListener(view -> viewLog());
        TimerRebootScheduler.WORKER.execute(() -> {
            TimerRebootScheduler.restore(getApplicationContext());
            handler.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    refreshStatus();
                }
            });
        });
    }

    private void setupSelectors() {
        String[] names = getResources().getStringArray(R.array.timer_reboot_weekday_names);
        LinearLayout parent = findViewById(R.id.timerWeekdayChecks);
        LinearLayout row = null;
        for (int display = 0; display < 7; display++) {
            if (display == 0 || display == 4) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                parent.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            int index = (display + 1) % 7;
            CheckBox check = new CheckBox(this);
            check.setText(names[index]);
            check.setTextSize(16);
            check.setPadding(0, 0, 0, 0);
            check.setMinWidth(0);
            check.setMinimumHeight(Math.round(48 * getResources().getDisplayMetrics().density));
            row.addView(check, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            check.setOnCheckedChangeListener((button, checked) -> markDirty());
            weekdays[index] = check;
        }
        List<String> days = new ArrayList<>();
        days.add(getString(R.string.timer_reboot_last_day));
        for (int value = 1; value <= 31; value++) {
            days.add(getString(R.string.timer_reboot_day_number, value));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.timer_reboot_day_item, days);
        adapter.setDropDownViewResource(R.layout.timer_reboot_day_item);
        monthDay.setAdapter(adapter);
        monthDay.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position != TimerRebootStore.plan(TimerRebootAty.this).monthDay) {
                    markDirty();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        types.setOnCheckedChangeListener((group, checkedId) -> {
            updateTypeFields();
            markDirty();
        });
        date.setOnClickListener(view -> new DatePickerDialog(this, (picker, y, m, d) -> {
            year = y;
            month = m + 1;
            day = d;
            updateDateTime();
            markDirty();
        }, year, month - 1, day).show());
        time.setOnClickListener(view -> {
            if (getSupportFragmentManager().findFragmentByTag(
                    TimerRebootTimeDialog.FRAGMENT_TAG) == null) {
                TimerRebootTimeDialog.newInstance(hour, minute).show(
                        getSupportFragmentManager(), TimerRebootTimeDialog.FRAGMENT_TAG);
            }
        });
    }

    @Override
    public void onTimeSelected(int selectedHour, int selectedMinute) {
        if (hour != selectedHour || minute != selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            updateDateTime();
            markDirty();
        }
    }

    private void loadDraft(Bundle state) {
        TimerRebootPlan p = TimerRebootStore.plan(this);
        suppress = true;
        year = state == null ? p.year : state.getInt("year", p.year);
        month = state == null ? p.month : state.getInt("month", p.month);
        day = state == null ? p.day : state.getInt("day", p.day);
        hour = state == null ? p.hour : state.getInt("hour", p.hour);
        minute = state == null ? p.minute : state.getInt("minute", p.minute);
        int type = state == null ? p.type : state.getInt("type", p.type);
        types.check(TYPE_IDS[Math.max(0, Math.min(3, type))]);
        int mask = state == null ? p.weekdays : state.getInt("weekdays", p.weekdays);
        for (int index = 0; index < 7; index++) {
            weekdays[index].setChecked((mask & (1 << index)) != 0);
        }
        monthDay.setSelection(state == null ? p.monthDay : state.getInt("monthDay", p.monthDay));
        dirty = state != null && state.getBoolean("dirty");
        suppress = false;
        updateTypeFields();
        updateDateTime();
    }

    private int selectedType() {
        for (int index = 0; index < TYPE_IDS.length; index++) {
            if (types.getCheckedRadioButtonId() == TYPE_IDS[index]) {
                return index;
            }
        }
        return TimerRebootPlan.DAILY;
    }

    private TimerRebootPlan draft() {
        int mask = 0;
        for (int index = 0; index < 7; index++) {
            if (weekdays[index].isChecked()) {
                mask |= 1 << index;
            }
        }
        return new TimerRebootPlan(selectedType(), hour, minute, year, month, day, mask,
                monthDay.getSelectedItemPosition());
    }

    private void updateTypeFields() {
        int type = selectedType();
        date.setVisibility(type == TimerRebootPlan.ONCE ? View.VISIBLE : View.GONE);
        findViewById(R.id.timerWeekdays).setVisibility(
                type == TimerRebootPlan.WEEKLY ? View.VISIBLE : View.GONE);
        findViewById(R.id.timerMonthPanel).setVisibility(
                type == TimerRebootPlan.MONTHLY ? View.VISIBLE : View.GONE);
    }

    private void updateDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        date.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
        time.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void markDirty() {
        if (!suppress) {
            dirty = true;
            dirtyLabel.setVisibility(View.VISIBLE);
        }
    }

    private void saveDraft() {
        TimerRebootPlan plan = draft();
        if (!plan.isValid()) {
            ToastUtils.error(getString(R.string.timer_reboot_select_weekday));
            return;
        }
        if (plan.type == TimerRebootPlan.ONCE && plan.nextAfter(System.currentTimeMillis(),
                TimeZone.getDefault(), TimerRebootStore.lastOccurrenceFor(this, plan)) == 0L) {
            ToastUtils.error(getString(R.string.timer_reboot_future_required));
            return;
        }
        busy = true;
        refreshStatus();
        TimerRebootScheduler.WORKER.execute(() -> {
            boolean result = TimerRebootScheduler.save(getApplicationContext(), plan);
            handler.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                busy = false;
                if (result) {
                    dirty = false;
                    ToastUtils.success(getString(R.string.timer_reboot_saved));
                } else {
                    ToastUtils.error(getString(R.string.timer_reboot_operation_failed));
                }
                refreshStatus();
            });
        });
    }

    private void changeEnabled(boolean checked) {
        TimerRebootPlan plan = TimerRebootStore.plan(this);
        if (checked && plan.type == TimerRebootPlan.ONCE
                && plan.nextAfter(System.currentTimeMillis(), TimeZone.getDefault(),
                TimerRebootStore.lastOccurrence(this)) == 0L) {
            ToastUtils.error(getString(R.string.timer_reboot_future_required));
            refreshStatus();
            return;
        }
        busy = true;
        refreshStatus();
        TimerRebootScheduler.WORKER.execute(() -> {
            boolean result = TimerRebootScheduler.enable(getApplicationContext(), checked);
            handler.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                busy = false;
                if (result) {
                    ToastUtils.success(getString(checked ? R.string.timer_reboot_enabled
                            : R.string.timer_reboot_disabled));
                } else {
                    ToastUtils.error(getString(R.string.timer_reboot_operation_failed));
                }
                refreshStatus();
            });
        });
    }

    private void refreshStatus() {
        boolean on = TimerRebootStore.enabled(this);
        boolean paused = TimerRebootStore.paused(this);
        boolean pending = TimerRebootStore.pending(this);
        suppress = true;
        enabled.setChecked(on);
        suppress = false;
        boolean editable = !pending && !busy;
        enabled.setEnabled(editable);
        date.setEnabled(editable);
        time.setEnabled(editable);
        monthDay.setEnabled(editable);
        save.setEnabled(editable);
        for (int id : TYPE_IDS) {
            findViewById(id).setEnabled(editable);
        }
        for (CheckBox check : weekdays) {
            check.setEnabled(editable);
        }
        dirtyLabel.setVisibility(dirty ? View.VISIBLE : View.GONE);
        errorPanel.setVisibility(paused ? View.VISIBLE : View.GONE);
        findViewById(R.id.timerResume).setEnabled(editable);
        error.setText(errorText(TimerRebootStore.prefs(this).getInt("error", 0)));
        long trigger = TimerRebootStore.next(this);
        if (pending) {
            status.setText(R.string.reboot_task_status_executing);
            countdown.setText(R.string.reboot_countdown_executing);
        } else if (paused) {
            status.setText(R.string.reboot_task_status_paused);
            countdown.setText(R.string.reboot_countdown_paused);
        } else if (!on) {
            boolean expired = TimerRebootStore.prefs(this).getBoolean("expired", false);
            boolean completed = TimerRebootStore.prefs(this).getBoolean("completed", false);
            status.setText(expired ? R.string.timer_reboot_expired : completed
                    ? R.string.timer_reboot_completed : R.string.reboot_task_status_disabled);
            countdown.setText(expired ? R.string.timer_reboot_expired : completed
                    ? R.string.timer_reboot_completed : R.string.reboot_countdown_disabled);
        } else {
            status.setText(R.string.reboot_task_status_running);
            countdown.setText(formatCountdown(Math.max(0L, trigger - System.currentTimeMillis())));
        }
        next.setText(on && !paused && !pending && trigger > 0L
                ? getString(R.string.timer_reboot_next, formatDate(trigger))
                : getString(R.string.timer_reboot_no_next));
        activeRule.setText(TimerRebootStore.configured(this)
                ? summary(TimerRebootStore.plan(this)) : "");
        if (pending) {
            lastResult.setText(R.string.timer_reboot_requested);
        } else {
            String result = TimerRebootStore.prefs(this).getString("lastResult", "");
            long wall = TimerRebootStore.prefs(this).getLong("lastResultTime", 0L);
            int text = "SUCCESS".equals(result) ? R.string.timer_reboot_success
                    : "FAILED".equals(result) ? R.string.timer_reboot_failed
                    : "UNCONFIRMED".equals(result) ? R.string.timer_reboot_unconfirmed
                    : R.string.timer_reboot_no_record;
            lastResult.setText(wall > 0L ? getString(R.string.timer_reboot_rule_time,
                    getString(text), formatDate(wall)) : getString(text));
        }
    }

    private int errorText(int code) {
        switch (code) {
            case TimerRebootStore.ERROR_STORAGE: return R.string.timer_reboot_error_storage;
            case TimerRebootStore.ERROR_ALARM: return R.string.timer_reboot_error_alarm;
            case TimerRebootStore.ERROR_REBOOT: return R.string.timer_reboot_error_reboot;
            case TimerRebootStore.ERROR_CONFIRM: return R.string.timer_reboot_error_confirm;
            default: return R.string.timer_reboot_error_state;
        }
    }

    private String summary(TimerRebootPlan p) {
        String clock = String.format(Locale.getDefault(), "%02d:%02d", p.hour, p.minute);
        if (p.type == TimerRebootPlan.MONTHLY) {
            return p.monthDay == TimerRebootPlan.LAST_DAY
                    ? getString(R.string.timer_reboot_last_day_summary, clock)
                    : getString(R.string.timer_reboot_monthly_summary, p.monthDay, clock);
        }
        String rule = getString(p.type == TimerRebootPlan.ONCE ? R.string.timer_reboot_once
                : p.type == TimerRebootPlan.DAILY ? R.string.timer_reboot_daily
                : R.string.timer_reboot_weekly);
        if (p.type == TimerRebootPlan.WEEKLY) {
            StringBuilder selected = new StringBuilder();
            String[] names = getResources().getStringArray(R.array.timer_reboot_weekday_names);
            for (int offset = 0; offset < 7; offset++) {
                int index = (offset + 1) % 7;
                if ((p.weekdays & (1 << index)) != 0) {
                    if (selected.length() > 0) {
                        selected.append(", ");
                    }
                    selected.append(names[index]);
                }
            }
            rule += " " + selected;
        } else if (p.type == TimerRebootPlan.ONCE) {
            rule += String.format(Locale.getDefault(), " %04d-%02d-%02d", p.year, p.month, p.day);
        }
        return getString(R.string.timer_reboot_rule_time, rule, clock);
    }

    private String formatCountdown(long milliseconds) {
        long seconds = (milliseconds + 999L) / 1000L;
        long days = seconds / 86400L;
        String clock = String.format(Locale.US, "%02d:%02d:%02d", (seconds / 3600L) % 24L,
                (seconds / 60L) % 60L, seconds % 60L);
        return days > 0L ? getString(R.string.timer_reboot_days_countdown, days, clock) : clock;
    }

    private String formatDate(long wall) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(wall));
    }

    private void viewLog() {
        TimerRebootScheduler.WORKER.execute(() -> {
            String content;
            try {
                content = TimerRebootLog.read(getApplicationContext()).toString(2);
            } catch (Exception readError) {
                content = getString(R.string.timer_reboot_log_read_failed);
            }
            String message = content;
            handler.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    new AlertDialog.Builder(this).setTitle(R.string.timer_reboot_latest)
                            .setMessage(message).setPositiveButton(android.R.string.ok, null).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        TimerRebootPlan p = draft();
        state.putInt("type", p.type);
        state.putInt("year", p.year);
        state.putInt("month", p.month);
        state.putInt("day", p.day);
        state.putInt("hour", p.hour);
        state.putInt("minute", p.minute);
        state.putInt("weekdays", p.weekdays);
        state.putInt("monthDay", p.monthDay);
        state.putBoolean("dirty", dirty);
        super.onSaveInstanceState(state);
    }
}
