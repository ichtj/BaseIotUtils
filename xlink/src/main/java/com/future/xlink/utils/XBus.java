package com.future.xlink.utils;

import org.greenrobot.eventbus.EventBus;

public final class XBus {

    public static void post(Carrier event) {
        EventBus.getDefault().post(event);
    }

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }
}