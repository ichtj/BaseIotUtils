package com.ichtj.basetools.reboot;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.ichtj.basetools.R;

import java.util.Locale;

public class TimerRebootNumberPicker extends NumberPicker {
    interface OnPrepareScrollListener {
        boolean onPrepareScroll();
    }

    private final EditText editor;
    private final int touchSlop;
    private OnPrepareScrollListener prepareScrollListener;
    private boolean blockedGesture;
    private boolean waitingForDrag;
    private float downY;

    public TimerRebootNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        EditText input = null;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) instanceof EditText) {
                input = (EditText) getChildAt(index);
                break;
            }
        }
        if (input == null) {
            throw new IllegalStateException("NumberPicker has no numeric editor");
        }
        editor = input;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setSaveEnabled(false);
        editor.setSaveEnabled(false);
        editor.setInputType(InputType.TYPE_CLASS_NUMBER);
        // The framework filter can silently turn a pasted "24" into a different hour.
        editor.setFilters(new InputFilter[0]);
        editor.setSelectAllOnFocus(true);
        editor.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                editor.selectAll();
            } else {
                commitInput();
            }
        });
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (TimerRebootTimeInput.parse(text, getMaxValue()) >= 0) {
                    editor.setError(null);
                }
            }
        });
    }

    void configure(int maximum, int value, int label, int imeAction) {
        setMinValue(0);
        setMaxValue(maximum);
        setWrapSelectorWheel(true);
        setFormatter(number -> String.format(Locale.getDefault(), "%02d", number));
        setValue(value);
        // The framework otherwise delays updating the editor until a fling finishes.
        setOnValueChangedListener((picker, previous, current) ->
                editor.setText(String.format(Locale.getDefault(), "%02d", current)));
        editor.setContentDescription(getContext().getString(label));
        editor.setImeOptions(imeAction | android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }

    EditText editor() {
        return editor;
    }

    void setOnPrepareScrollListener(OnPrepareScrollListener listener) {
        prepareScrollListener = listener;
    }

    boolean commitInput() {
        int value = TimerRebootTimeInput.parse(editor.getText(), getMaxValue());
        if (value < 0) {
            editor.setError(getContext().getString(getMaxValue() == 23
                    ? R.string.timer_reboot_hour_error : R.string.timer_reboot_minute_error));
            return false;
        }
        setValue(value);
        editor.setText(String.format(Locale.getDefault(), "%02d", value));
        editor.setError(null);
        return true;
    }

    void focusInput() {
        editor.setVisibility(View.VISIBLE);
        editor.requestFocus();
        editor.selectAll();
    }

    private boolean prepareScroll() {
        return prepareScrollListener == null ? commitInput()
                : prepareScrollListener.onPrepareScroll();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        String input = editor.getText().toString();
        int selectionStart = editor.getSelectionStart();
        int selectionEnd = editor.getSelectionEnd();
        super.onLayout(changed, left, top, right, bottom);
        // NumberPicker resets its editor during layout, including when the IME resizes it.
        if (!input.contentEquals(editor.getText())) {
            editor.setText(input);
            if (selectionStart >= 0 && selectionEnd >= 0) {
                editor.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            downY = event.getY();
            waitingForDrag = editor.hasFocus()
                    && downY >= editor.getTop() && downY <= editor.getBottom();
            blockedGesture = !waitingForDrag && !prepareScroll();
        } else if (action == MotionEvent.ACTION_MOVE && waitingForDrag
                && Math.abs(event.getY() - downY) > touchSlop) {
            waitingForDrag = false;
            if (!prepareScroll()) {
                // Cancel before NumberPicker can replace an invalid edit with a wheel value.
                MotionEvent cancel = MotionEvent.obtain(event);
                cancel.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(cancel);
                cancel.recycle();
                focusInput();
                blockedGesture = true;
            }
        }
        if (blockedGesture) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)
                && !prepareScroll()) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
