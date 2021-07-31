package com.future.xlink.utils;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * corePoolSize     指定了线程池中的线程数量，它的数量决定了添加的任务是开辟新的线程去执行，还是放到workQueue任务队列中去
 * maximumPoolSize  指定了线程池中的最大线程数量，这个参数会根据你使用的workQueue任务队列的类型，决定线程池会开辟的最大线程数量；
 *      当线程数=maxPoolSize，且任务队列已满时，线程池会拒绝处理任务而抛出异常
 * keepAliveTime    当线程池中空闲线程数量超过corePoolSize时，多余的线程会在多长时间内被销毁
 *
 */
public class ThreadPool {
    private static final String TAG = "ThreadPool";
    public static class Builder {
        private static ExecutorService executors = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue <>(), new ThreadFactoryImpl());
    }

    private static class ThreadFactoryImpl implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ThreadFactoryImpl() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    public static void add(Runnable r) {
        Builder.executors.execute(r);
    }

    public static ExecutorService get() {
        return Builder.executors;
    }
}