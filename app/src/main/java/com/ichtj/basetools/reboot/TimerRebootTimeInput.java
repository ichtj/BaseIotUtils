package com.ichtj.basetools.reboot;

final class TimerRebootTimeInput {
    private TimerRebootTimeInput() {
    }

    static int parse(CharSequence text, int maximum) {
        if (text == null || text.length() == 0 || text.length() > 2) {
            return -1;
        }
        int value = 0;
        for (int index = 0; index < text.length(); index++) {
            int digit = Character.digit(text.charAt(index), 10);
            if (digit < 0) {
                return -1;
            }
            value = value * 10 + digit;
        }
        return value <= maximum ? value : -1;
    }
}
