package com.ichtj.basetools.reboot;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ichtj.basetools.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Detached picker checks: never start the reboot Activity, schedule an alarm, or reboot. */
@RunWith(AndroidJUnit4.class)
public class TimerRebootTimePickerTest {
    private View content;
    private TimerRebootNumberPicker hours;
    private TimerRebootNumberPicker minutes;

    @Before
    public void setUp() {
        onMain(() -> {
            Context context = new ContextThemeWrapper(InstrumentationRegistry
                    .getInstrumentation().getTargetContext(), R.style.TimerRebootTimeDialogTheme);
            content = LayoutInflater.from(context).inflate(R.layout.dialog_timer_reboot_time, null);
            hours = content.findViewById(R.id.timerTimeHour);
            minutes = content.findViewById(R.id.timerTimeMinute);
            hours.configure(23, 7, R.string.timer_reboot_hour, EditorInfo.IME_ACTION_NEXT);
            minutes.configure(59, 25, R.string.timer_reboot_minute, EditorInfo.IME_ACTION_DONE);
            content.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.AT_MOST));
            content.layout(0, 0, content.getMeasuredWidth(), content.getMeasuredHeight());
        });
    }

    @Test
    public void typingDoesNotMoveTheWheelUntilCommitted() {
        onMain(() -> {
            hours.focusInput();
            hours.editor().setText("1");
            assertEquals(7, hours.getValue());
            hours.editor().append("8");
            assertEquals(7, hours.getValue());
            assertTrue(hours.commitInput());
            assertEquals(18, hours.getValue());
        });
    }

    @Test
    public void singleDigitIsPaddedWhenCommitted() {
        onMain(() -> {
            minutes.editor().setText("8");
            assertTrue(minutes.commitInput());
            assertEquals(8, minutes.getValue());
            assertEquals(String.format(Locale.getDefault(), "%02d", 8),
                    minutes.editor().getText().toString());
        });
    }

    @Test
    public void emptyAndOutOfRangeValuesRemainVisibleForCorrection() {
        onMain(() -> {
            for (String text : new String[]{"", "24", "99", "180"}) {
                hours.editor().setText(text);
                assertEquals(text, hours.editor().getText().toString());
                assertFalse(hours.commitInput());
                assertEquals(7, hours.getValue());
                assertEquals(text, hours.editor().getText().toString());
                assertNotNull(hours.editor().getError());
            }
            minutes.editor().setText("60");
            assertFalse(minutes.commitInput());
            assertEquals(25, minutes.getValue());
        });
    }

    @Test
    public void correctingAnInvalidValueClearsItsError() {
        onMain(() -> {
            hours.editor().setText("24");
            assertFalse(hours.commitInput());
            hours.editor().setText("18");
            assertNull(hours.editor().getError());
            assertTrue(hours.commitInput());
            assertEquals(18, hours.getValue());
        });
    }

    @Test
    public void layoutDoesNotReplaceAnUncommittedEditWithTheOldWheelValue() {
        onMain(() -> {
            hours.focusInput();
            hours.editor().setText("24");
            hours.layout(0, 0, hours.getWidth() + 1, hours.getHeight() + 1);
            assertEquals("24", hours.editor().getText().toString());
            assertEquals(7, hours.getValue());
        });
    }

    @Test
    public void losingFocusCommitsTheCurrentText() {
        onMain(() -> {
            hours.focusInput();
            hours.editor().setText("18");
            hours.editor().clearFocus();
            assertEquals(18, hours.getValue());
        });
    }

    @Test
    public void invalidTextIsNotReplacedWithThePreviousValueOnFocusLoss() {
        onMain(() -> {
            hours.focusInput();
            hours.editor().setText("");
            hours.editor().clearFocus();
            assertEquals("", hours.editor().getText().toString());
            assertNotNull(hours.editor().getError());
            assertEquals(7, hours.getValue());
        });
    }

    @Test
    public void scrollStartCommitsTheTypedValue() {
        onMain(() -> {
            hours.focusInput();
            hours.editor().setText("18");
            dispatch(hours, MotionEvent.ACTION_DOWN, 1);
            assertEquals(18, hours.getValue());
            dispatch(hours, MotionEvent.ACTION_CANCEL, 1);
        });
    }

    @Test
    public void invalidInputBlocksWheelTouchAndDpadChanges() {
        onMain(() -> {
            hours.editor().setText("24");
            dispatch(hours, MotionEvent.ACTION_DOWN, 1);
            dispatch(hours, MotionEvent.ACTION_MOVE, hours.getHeight() - 1);
            dispatch(hours, MotionEvent.ACTION_UP, hours.getHeight() - 1);
            assertEquals("24", hours.editor().getText().toString());
            assertEquals(7, hours.getValue());
            assertTrue(hours.dispatchKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)));
            assertEquals(7, hours.getValue());
        });
    }

    @Test
    public void invalidHourCanBlockMinuteScrollingThroughTheSharedGate() {
        onMain(() -> {
            hours.editor().setText("24");
            minutes.setOnPrepareScrollListener(() -> hours.commitInput() && minutes.commitInput());
            dispatch(minutes, MotionEvent.ACTION_DOWN, 1);
            dispatch(minutes, MotionEvent.ACTION_MOVE, minutes.getHeight() - 1);
            dispatch(minutes, MotionEvent.ACTION_UP, minutes.getHeight() - 1);
            assertEquals(25, minutes.getValue());
            assertEquals("24", hours.editor().getText().toString());
        });
    }

    @Test
    public void minuteWrapsWithoutCarryingIntoTheHour() {
        onMain(() -> {
            hours.setValue(3);
            minutes.setValue(59);
            assertTrue(hours.getWrapSelectorWheel());
            assertTrue(minutes.getWrapSelectorWheel());
            minutes.scrollBy(0, -minutes.getHeight() / 3);
            assertEquals(0, minutes.getValue());
            assertEquals(3, hours.getValue());
            minutes.scrollBy(0, minutes.getHeight() / 3);
            assertEquals(59, minutes.getValue());
            assertEquals(3, hours.getValue());
        });
    }

    @Test
    public void hourWrapsIndependentlyInBothDirections() {
        onMain(() -> {
            hours.setValue(23);
            hours.scrollBy(0, -hours.getHeight() / 3);
            assertEquals(0, hours.getValue());
            assertEquals(25, minutes.getValue());
            hours.scrollBy(0, hours.getHeight() / 3);
            assertEquals(23, hours.getValue());
        });
    }

    private static void dispatch(View view, int action, float y) {
        MotionEvent event = MotionEvent.obtain(0, 0, action, view.getWidth() / 2f, y, 0);
        view.dispatchTouchEvent(event);
        event.recycle();
    }

    private static void onMain(Runnable action) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }
}
