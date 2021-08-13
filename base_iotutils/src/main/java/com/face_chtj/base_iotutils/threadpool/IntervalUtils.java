package com.face_chtj.base_iotutils.threadpool;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 执行一个周期任务(定时任务)
 */
public class IntervalUtils {
    private static final String TAG = "SingletonDisposable";

    private IntervalUtils() {
    }

    private static class Builder {
        static HashMap<String, Disposable> map = new HashMap<>();
    }

    /**
     * 添加定时任务
     *
     * @param tag          标志
     * @param period       隔多久执行一次
     * @param unit         设定时间单位
     * @param consumer     回调
     */
    public static void add(@NonNull String tag, long period, TimeUnit unit, Consumer<Long> consumer) {
        add(tag, 0, period, unit, consumer);
    }

    /**
     * 添加定时任务
     *
     * @param tag          标志
     * @param initialDelay 延迟多久执行
     * @param period       隔多久执行一次
     * @param unit         设定时间单位
     * @param consumer     回调
     */
    public static void add(@NonNull String tag, long initialDelay, long period, TimeUnit unit, Consumer<Long> consumer) {
        add(tag, initialDelay, period, unit, consumer, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                KLog.d(TAG,"throwable="+throwable.getMessage());
            }
        });
    }

    /**
     * 添加定时任务
     *
     * @param tag          标志
     * @param initialDelay 延迟多久执行
     * @param period       隔多久执行一次
     * @param unit         设定时间单位
     * @param consumer     回调
     * @param tConsumer     异常
     */
    public static void add(@NonNull String tag, long initialDelay, long period, TimeUnit unit, Consumer<Long> consumer,Consumer<Throwable> tConsumer) {
        //判断任务是否存在
        if (!StringUtils.isEmpty(tag) && !Builder.map.containsKey(tag)) {
            Disposable disposable = Observable.interval(initialDelay, period, unit)
                    .subscribe(consumer, tConsumer);
            //不存在的时候才添加进任务列表
            Builder.map.put(tag, disposable);
        } else {
            KLog.e(TAG, "There is no task that has been added, and the added tag is empty");
        }
    }

    /**
     * 按照标志清除任务
     *
     * @param tag 标志
     */
    public static void clear(@NonNull String tag) {
        if (Builder.map.containsKey(tag)) {
            Disposable disposable = Builder.map.get(tag);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    /**
     * 清除并停止所有的任务
     */
    public static void clearAll() {
        KLog.d(TAG, "clearAll:>=");
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
            KLog.d(TAG, "clearAll:>size=" + Builder.map.size());
        }
    }
}
