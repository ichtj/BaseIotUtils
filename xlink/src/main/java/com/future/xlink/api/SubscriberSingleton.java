package com.future.xlink.api;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class SubscriberSingleton {

    private SubscriberSingleton() {
    }
    private static class Builder {
        static HashMap <String, List <Disposable>> map = new HashMap <>();
    }

    public static void add(@NonNull String tag, @NonNull Disposable s) {
        if (Builder.map.containsKey(tag)) {
            List <Disposable> subscribers = Builder.map.get(tag);
            subscribers.add(s);
        } else {
            List <Disposable> temp = new ArrayList <>();
            temp.add(s);
            Builder.map.put(tag, temp);
        }
    }

    public static void clear(@NonNull String tag) {
        if (Builder.map.containsKey(tag)) {
            List <Disposable> observers = Builder.map.get(tag);
            for (Disposable s : observers) {
                if (s != null && !s.isDisposed()) {
                    s.dispose();
                }
            }
        }
    }
}
