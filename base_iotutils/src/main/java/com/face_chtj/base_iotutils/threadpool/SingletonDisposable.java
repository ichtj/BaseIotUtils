package com.wave_chtj.example.util;

import com.face_chtj.base_iotutils.KLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class SingletonDisposable {
    private static final String TAG = "SingletonDisposable";

    private SingletonDisposable() {
    }

    private static class Builder {
        static HashMap<String, Disposable> map = new HashMap<>();
    }

    public static void add(@NonNull String tag, @NonNull Disposable s) {
        KLog.d(TAG,"add:>="+tag);
        if (!Builder.map.containsKey(tag)) {
            Builder.map.put(tag, s);
            KLog.d(TAG,"add:>size="+ Builder.map.size());
        }
    }

    public static void clear(@NonNull String tag) {
        if (Builder.map.containsKey(tag)) {
            Disposable disposable = Builder.map.get(tag);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public static void clearAll() {
        KLog.d(TAG,"clearAll:>=");
        Iterator<Map.Entry<String, Disposable>> it = Builder.map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Disposable> itEntry = it.next();
            Object itKey = itEntry.getKey();
            Disposable disposable = itEntry.getValue();
            KLog.d(TAG, "dispose and remove key:" + itKey + " value:" + disposable);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            it.remove();
            KLog.d(TAG,"clearAll:>size="+ Builder.map.size());
        }
    }
}
