package com.ichtj.basetools.reboot;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.ichtj.basetools.R;

public class TimerRebootTimeDialog extends DialogFragment {
    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    static final String FRAGMENT_TAG = "ichtj_timer_time_picker";
    private TimerRebootNumberPicker hours;
    private TimerRebootNumberPicker minutes;
    private View content;

    static TimerRebootTimeDialog newInstance(int hour, int minute) {
        TimerRebootTimeDialog fragment = new TimerRebootTimeDialog();
        Bundle arguments = new Bundle();
        arguments.putInt("hour", hour);
        arguments.putInt("minute", minute);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),
                R.style.TimerRebootTimeDialogTheme);
        content = LayoutInflater.from(builder.getContext())
                .inflate(R.layout.dialog_timer_reboot_time, null);
        hours = content.findViewById(R.id.timerTimeHour);
        minutes = content.findViewById(R.id.timerTimeMinute);
        Bundle values = state == null ? getArguments() : state;
        hours.configure(23, values.getInt("hour"), R.string.timer_reboot_hour,
                EditorInfo.IME_ACTION_NEXT);
        minutes.configure(59, values.getInt("minute"), R.string.timer_reboot_minute,
                EditorInfo.IME_ACTION_DONE);
        if (state != null) {
            hours.editor().setText(state.getString("hourInput"));
            minutes.editor().setText(state.getString("minuteInput"));
        }
        hours.setOnPrepareScrollListener(this::prepareScroll);
        minutes.setOnPrepareScrollListener(this::prepareScroll);
        hours.editor().setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_NEXT) {
                if (commitInputs()) {
                    minutes.focusInput();
                }
                return true;
            }
            return false;
        });
        minutes.editor().setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) {
                prepareScroll();
                return true;
            }
            return false;
        });
        return builder.setTitle(R.string.timer_reboot_time).setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        content.requestFocus();
        AlertDialog dialog = (AlertDialog) getDialog();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (prepareScroll()) {
                if (getActivity() instanceof OnTimeSelectedListener) {
                    ((OnTimeSelectedListener) getActivity())
                            .onTimeSelected(hours.getValue(), minutes.getValue());
                }
                dismiss();
            }
        });
    }

    private boolean commitInputs() {
        boolean validHour = hours.commitInput();
        boolean validMinute = minutes.commitInput();
        if (!validHour || !validMinute) {
            (validHour ? minutes : hours).focusInput();
            return false;
        }
        return true;
    }

    private boolean prepareScroll() {
        if (!commitInputs()) {
            return false;
        }
        content.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(content.getWindowToken(), 0);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("hour", hours.getValue());
        state.putInt("minute", minutes.getValue());
        state.putString("hourInput", hours.editor().getText().toString());
        state.putString("minuteInput", minutes.editor().getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hours = null;
        minutes = null;
        content = null;
    }
}
