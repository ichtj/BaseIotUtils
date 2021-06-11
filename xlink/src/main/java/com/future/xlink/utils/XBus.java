package com.future.xlink.utils;

import com.future.xlink.logs.Log4J;

import org.greenrobot.eventbus.EventBus;

public final class XBus {
    private static final Class TAG = XBus.class.getClass();
    public static void post(Carrier event) {
        EventBus.getDefault().post(event);
    }

    public static void register(Object subscriber) {
        Log4J.info(TAG, "register", "register xbus");
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        Log4J.info(TAG, "unregister", "unregister xbus");
        EventBus.getDefault().unregister(subscriber);
    }
}