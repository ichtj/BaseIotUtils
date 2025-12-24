package com.face_chtj.base_iotutils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理 管理整个项目中所有的线程，所以不能有多个实例对象
 * @author chtj
 */
public class TPoolUtils {
    private static TPoolUtils sTPoolUtils = new TPoolUtils();

    /*线程池维护线程的最少数量 核心线程数*/
    private static final int SIZE_CORE_POOL = 3;

    /*线程池维护线程的最大数量 最大线程数*/
    private static final int SIZE_MAX_POOL = 4;

    /*线程池维护线程所允许的空闲时间*/
    private static final int TIME_KEEP_ALIVE = 5000;

    /*线程池所使用的缓冲队列大小*/
    private static final int SIZE_WORK_QUEUE = 500;

    /*任务调度周期*/
    private static final int PERIOD_TASK_QOS = 1000;

    /**
     * 线程池单例创建方法
     */
    public static TPoolUtils newInstance() {
        return sTPoolUtils;
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
     * 将缓冲队列中的任务重新加载到线程池
     */
    private final Runnable mAccessBufferThread = new Runnable() {
        @Override
        public void run() {
            if (hasMoreAcquire()) {
                mThreadPool.execute(mTaskQueue.poll());
            }
        }
    };

    /**
     * 创建一个调度线程池
     */
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

    /**
     * 通过调度线程周期性的执行缓冲队列中任务
     */
    protected final ScheduledFuture<?> mTaskHandler = scheduler.scheduleAtFixedRate(mAccessBufferThread, 0,
            PERIOD_TASK_QOS, TimeUnit.MILLISECONDS);

    /**
     * 线程池
     */
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL,
            TIME_KEEP_ALIVE, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(SIZE_WORK_QUEUE), mHandler);

    /**
     * 将构造方法访问修饰符设为私有，禁止任意实例化。
     */
    private TPoolUtils() {
    }

    public void perpare() {
        if (mThreadPool.isShutdown() && !mThreadPool.prestartCoreThread()) {
            @SuppressWarnings("unused")
            int startThread = mThreadPool.prestartAllCoreThreads();
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
    public void addExecuteTask(Runnable task) {
        if (task != null) {
            mThreadPool.execute(task);
        }
    }

    protected boolean isTaskEnd() {
        if (mThreadPool.getActiveCount() == 0) {
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
        mThreadPool.shutdown();
    }

    /**
     *  执行该方法，线程池的状态立刻变成STOP状态，并试图停止所有正在执行的线程，不再处理还在池队列中等待的任务，当然，它会返回那些未执行的任务
     *  它试图终止线程的方法是通过调用Thread.interrupt()方法来实现的，但是大家知道，这种方法的作用有限，如果线程中没有sleep 、wait、Condition、定时锁等应用, interrupt()方法是无法中断当前的线程的
     *  并不代表线程池就一定立即就能退出，它可能必须要等待所有正在执行的任务都执行完成了才能退出。
     */
    public void shutdownNow() {
        mTaskQueue.clear();
        mThreadPool.shutdownNow();
    }
}
