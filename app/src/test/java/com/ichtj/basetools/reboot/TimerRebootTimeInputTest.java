package com.ichtj.basetools.reboot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimerRebootTimeInputTest {
    @Test
    public void acceptsHourAndMinuteBoundaries() {
        assertEquals(0, TimerRebootTimeInput.parse("00", 23));
        assertEquals(23, TimerRebootTimeInput.parse("23", 23));
        assertEquals(0, TimerRebootTimeInput.parse("00", 59));
        assertEquals(59, TimerRebootTimeInput.parse("59", 59));
    }

    @Test
    public void acceptsOneDigitAndLeadingZeroWithoutChangingItsValue() {
        assertEquals(8, TimerRebootTimeInput.parse("8", 23));
        assertEquals(8, TimerRebootTimeInput.parse("08", 23));
        assertEquals(1, TimerRebootTimeInput.parse("1", 59));
    }

    @Test
    public void rejectsMissingOrOutOfRangeInputInsteadOfUsingAnOldValue() {
        assertEquals(-1, TimerRebootTimeInput.parse(null, 23));
        assertEquals(-1, TimerRebootTimeInput.parse("", 23));
        assertEquals(-1, TimerRebootTimeInput.parse("24", 23));
        assertEquals(-1, TimerRebootTimeInput.parse("99", 23));
        assertEquals(-1, TimerRebootTimeInput.parse("60", 59));
    }

    @Test
    public void rejectsSignsWhitespaceTextAndMoreThanTwoDigits() {
        for (String text : new String[]{"-1", "+1", " 1", "1 ", "a", "1.5", "008", "100"}) {
            assertEquals(text, -1, TimerRebootTimeInput.parse(text, 59));
        }
    }

    @Test
    public void acceptsLocalizedDecimalDigitsUsedByThePickerFormatter() {
        assertEquals(18, TimerRebootTimeInput.parse("\u0661\u0668", 23));
        assertEquals(59, TimerRebootTimeInput.parse("\u06f5\u06f9", 59));
    }
}
