package com.face_chtj.base_iotutils.threadpool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理 管理整个项目中所有的线程，所以不能有多个实例对象
 * @author chtj
 */
public class ScheduledTPoolUtils {
    private static ScheduledTPoolUtils sThreadPoolManager = new ScheduledTPoolUtils();

    /*线程池维护线程的最少数量 核心线程数*/
    private static final int SIZE_CORE_POOL = 3;

    /**
     * 线程池单例创建方法
     */
    public static ScheduledTPoolUtils newInstance() {
        return sThreadPoolManager;
    }

    /**
     * 任务缓冲队列
     */
    private final Queue<Runnable> mTaskQueue = new LinkedList<Runnable>();

    /**
     * 线程池超出界线时将任务加入缓冲队列
     */
    private final RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            mTaskQueue.offer(task);
        }
    };


    /**
     * 线程池
     */
    private final ScheduledThreadPoolExecutor scheduledThread = new ScheduledThreadPoolExecutor(SIZE_CORE_POOL,mHandler);

    /**
     * 将构造方法访问修饰符设为私有，禁止任意实例化。
     */
    private ScheduledTPoolUtils() {
    }

    public void perpare() {
        if (scheduledThread.isShutdown() && !scheduledThread.prestartCoreThread()) {
            @SuppressWarnings("unused")
            int startThread = scheduledThread.prestartAllCoreThreads();
        }
    }

    /**
     * 消息队列检查方法
     */
    private boolean hasMoreAcquire() {
        return !mTaskQueue.isEmpty();
    }

    /**
     * 向线程池中添加任务方法
     */
    public void addExecuteTask(Runnable task, int initialDelay, int period) {
        if (task != null) {
            scheduledThread.scheduleAtFixedRate(task,initialDelay, period, TimeUnit.SECONDS);
        }
    }

    protected boolean isTaskEnd() {
        if (scheduledThread.getActiveCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 当线程池调用该方法时,线程池的状态则立刻变成SHUTDOWN状态。
     * 此时，则不能再往线程池中添加任何任务，否则将会抛出RejectedExecutionException异常。
     * 但是，此时线程池不会立刻退出，直到添加到线程池中的任务都已经处理完成，才会退出
     */
    public void shutdown() {
        mTaskQueue.clear();
        scheduledThread.shutdown();
    }

    /**
     *  执行该方法，线程池的状态立刻变成STOP状态，并试图停止所有正在执行的线程，不再处理还在池队列中等待的任务，当然，它会返回那些未执行的任务
     *  它试图终止线程的方法是通过调用Thread.interrupt()方法来实现的，但是大家知道，这种方法的作用有限，如果线程中没有sleep 、wait、Condition、定时锁等应用, interrupt()方法是无法中断当前的线程的
     *  并不代表线程池就一定立即就能退出，它可能必须要等待所有正在执行的任务都执行完成了才能退出。
     */
    public void shutdownNow() {
        mTaskQueue.clear();
        scheduledThread.shutdownNow();
    }
}
