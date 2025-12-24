package com.face_chtj.base_iotutils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public class LogView extends RecyclerView {

    private static final String TAG = "LogView"; // 日志标签
    private boolean enableDebugLog = false;      // 调试日志开关

    private final LogAdapter logAdapter;
    private final LinkedList<String> displayLogs = new LinkedList<>(); // 高效队列
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // 配置参数
    private boolean autoScroll;
    private int maxLogs;
    private int textColor;
    private float textSizeSp;

    // 写入缓冲区
    private final LinkedList<String> writeBuffer = new LinkedList<>();
    private final Object bufferLock = new Object();

    // 智能刷新控制
    private static final int BATCH_SIZE = 50;              // 积累 50 条触发刷新
    private static final long NORMAL_DELAY_MS = 16;        // 正常延迟 16ms (60fps)
    private static final long SLOW_DELAY_MS = 100;         // 降级延迟 100ms (10fps)
    private static final long MIN_REFRESH_INTERVAL = 16;   // 最小刷新间隔

    // 内存保护
    private static final int MEMORY_WARNING_SIZE = 5000;   // 超过 5000 条降级
    private static final int MAX_BUFFER_SIZE = 1000;       // 缓冲区最大 1000 条

    private volatile boolean isDestroyed = false;
    private volatile boolean isFlushScheduled = false;
    private volatile long lastRefreshTime = 0;
    private volatile boolean isLowMemoryMode = false;      // 低内存模式标志

    private Runnable flushRunnable;

    // 统计数据
    private long totalAppendCount = 0;      // 累计追加次数
    private long totalFlushCount = 0;       // 累计刷新次数
    private long lastStatTime = 0;          // 上次统计时间
    private static final long STAT_INTERVAL = 10000; // 每 10 秒输出一次统计

    // 短日志累积
    private final StringBuilder shortLogBuffer = new StringBuilder();
    private static final int SHORT_LOG_MAX_LEN = 200; // 短日志累积阈值
    private long lastFlushShortLogTime = 0;
    private static final long MAX_SHORT_LOG_WAIT_MS = 1000; // 最大等待 1 秒

    public LogView(@NonNull Context context) {
        this(context, null);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LogView);
        textColor = ta.getColor(R.styleable.LogView_logTextColor, Color.WHITE);
        textSizeSp = ta.getDimension(R.styleable.LogView_logTextSize, 14f);
        maxLogs = ta.getInt(R.styleable.LogView_logMaxCount, 1000);
        autoScroll = ta.getBoolean(R.styleable.LogView_logAutoScroll, true);
        ta.recycle();

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);

        RecycledViewPool pool = getRecycledViewPool();
        pool.setMaxRecycledViews(0, 20);

        setItemAnimator(null);
        setHasFixedSize(true);

        logAdapter = new LogAdapter(displayLogs, textColor, textSizeSp);
        setAdapter(logAdapter);

        flushRunnable = () -> {
            isFlushScheduled = false;
            flushToDisplay();
        };

        lastStatTime = System.currentTimeMillis();

        Log.i(TAG, "LogView 初始化完成 [maxLogs=" + maxLogs + ", autoScroll=" + autoScroll + "]");
    }

    // ===================== 核心 API =====================

    public void setInitialLogs(List<String> initialLogs) {
        if (isDestroyed) return;

        synchronized (bufferLock) {
            displayLogs.clear();
            writeBuffer.clear();

            if (initialLogs != null && !initialLogs.isEmpty()) {
                int start = Math.max(0, initialLogs.size() - maxLogs);
                displayLogs.addAll(initialLogs.subList(start, initialLogs.size()));
            }
        }

        uiHandler.post(() -> {
            if (!isDestroyed) {
                logAdapter.notifyDataSetChanged();
                if (autoScroll) scrollToBottom();
            }
        });

        Log.i(TAG, "设置初始日志 [count=" + (initialLogs != null ? initialLogs.size() : 0) + "]");
    }

    public void appendLog(String text) {
        if (text == null || isDestroyed) return;

        boolean shouldFlush;
        synchronized (bufferLock) {
            if (writeBuffer.size() >= MAX_BUFFER_SIZE) {
                writeBuffer.removeFirst();
                if (enableDebugLog) {
                    Log.w(TAG, "缓冲区溢出,丢弃最旧日志 [bufferSize=" + MAX_BUFFER_SIZE + "]");
                }
            }

            writeBuffer.add(text);
            totalAppendCount++;
            shouldFlush = writeBuffer.size() >= BATCH_SIZE;

            checkMemoryStatus();
            printStatistics();
        }

        if (shouldFlush) {
            flushWithRateLimit();
        } else {
            scheduleFlush();
        }
    }

    private void checkMemoryStatus() {
        int totalSize = displayLogs.size() + writeBuffer.size();
        if (totalSize > MEMORY_WARNING_SIZE && !isLowMemoryMode) {
            isLowMemoryMode = true;
            int removeCount = displayLogs.size() / 2;
            for (int i = 0; i < removeCount && !displayLogs.isEmpty(); i++) {
                displayLogs.removeFirst();
            }
            Log.w(TAG, "⚠️ 进入低内存模式 [totalSize=" + totalSize + ", removed=" + removeCount + ", 刷新频率降至 10fps]");
        } else if (totalSize < MEMORY_WARNING_SIZE / 2 && isLowMemoryMode) {
            isLowMemoryMode = false;
            Log.i(TAG, "✅ 退出低内存模式 [totalSize=" + totalSize + ", 刷新频率恢复 60fps]");
        }
    }

    private void flushWithRateLimit() {
        if (isDestroyed) return;
        long now = System.currentTimeMillis();
        long timeSinceLastRefresh = now - lastRefreshTime;

        uiHandler.removeCallbacks(flushRunnable);
        isFlushScheduled = false;

        long minInterval = isLowMemoryMode ? SLOW_DELAY_MS : MIN_REFRESH_INTERVAL;

        if (timeSinceLastRefresh >= minInterval) {
            isFlushScheduled = true;
            uiHandler.post(flushRunnable);
        } else {
            long delay = minInterval - timeSinceLastRefresh;
            isFlushScheduled = true;
            uiHandler.postDelayed(flushRunnable, delay);
        }
    }

    private void scheduleFlush() {
        if (isDestroyed || isFlushScheduled) return;
        isFlushScheduled = true;
        long delay = isLowMemoryMode ? SLOW_DELAY_MS : NORMAL_DELAY_MS;
        uiHandler.postDelayed(flushRunnable, delay);
    }

    private void flushToDisplay() {
        if (isDestroyed) return;

        int transferred = 0;
        boolean hasRemoved = false;

        synchronized (bufferLock) {
            if (writeBuffer.isEmpty() && shortLogBuffer.length() == 0) return;

            long now = System.currentTimeMillis();

            while (!writeBuffer.isEmpty()) {
                String log = writeBuffer.removeFirst();

                if (log.length() <= SHORT_LOG_MAX_LEN) {
                    shortLogBuffer.append(log).append("\n");

                    // 累积到阈值再提交
                    if (shortLogBuffer.length() >= SHORT_LOG_MAX_LEN) {
                        if (displayLogs.size() >= maxLogs) {
                            displayLogs.removeFirst();
                            hasRemoved = true;
                        }
                        displayLogs.add(shortLogBuffer.toString());
                        shortLogBuffer.setLength(0);
                        transferred++;
                        lastFlushShortLogTime = now;
                    }
                } else {
                    // 长日志先提交短日志缓存
                    if (shortLogBuffer.length() > 0) {
                        if (displayLogs.size() >= maxLogs) {
                            displayLogs.removeFirst();
                            hasRemoved = true;
                        }
                        displayLogs.add(shortLogBuffer.toString());
                        shortLogBuffer.setLength(0);
                        transferred++;
                        lastFlushShortLogTime = now;
                    }

                    if (displayLogs.size() >= maxLogs) {
                        displayLogs.removeFirst();
                        hasRemoved = true;
                    }
                    displayLogs.add(log);
                    transferred++;
                }
            }

            // 超过最大等待时间，强制提交短日志
            if (shortLogBuffer.length() > 0 && now - lastFlushShortLogTime >= MAX_SHORT_LOG_WAIT_MS) {
                if (displayLogs.size() >= maxLogs) {
                    displayLogs.removeFirst();
                    hasRemoved = true;
                }
                displayLogs.add(shortLogBuffer.toString());
                shortLogBuffer.setLength(0);
                transferred++;
                lastFlushShortLogTime = now;
            }

            totalFlushCount++;
        }

        lastRefreshTime = System.currentTimeMillis();

        if (!isDestroyed && transferred > 0) {
            if (hasRemoved || transferred > maxLogs / 2 || isLowMemoryMode) {
                logAdapter.notifyDataSetChanged();
            } else {
                int insertPosition = displayLogs.size() - transferred;
                if (insertPosition >= 0) {
                    logAdapter.notifyItemRangeInserted(insertPosition, transferred);
                } else {
                    logAdapter.notifyDataSetChanged();
                }
            }

            if (autoScroll) scrollToBottom();
        }
    }

    private void scrollToBottom() {
        if (!isDestroyed && logAdapter.getItemCount() > 0) {
            scrollToPosition(logAdapter.getItemCount() - 1);
        }
    }

    public void clearLogs() {
        if (isDestroyed) return;

        int beforeCount;
        synchronized (bufferLock) {
            beforeCount = displayLogs.size() + writeBuffer.size();
            displayLogs.clear();
            writeBuffer.clear();
            shortLogBuffer.setLength(0);
            isLowMemoryMode = false;
            uiHandler.removeCallbacks(flushRunnable);
            isFlushScheduled = false;
        }

        uiHandler.post(() -> {
            if (!isDestroyed) {
                logAdapter.notifyDataSetChanged();
            }
        });

        Log.i(TAG, "清空日志 [清除=" + beforeCount + "条]");
    }

    public void forceFlush() {
        if (isDestroyed) return;
        uiHandler.removeCallbacks(flushRunnable);
        isFlushScheduled = true;
        uiHandler.post(flushRunnable);

        if (enableDebugLog) {
            Log.d(TAG, "强制刷新 [bufferSize=" + writeBuffer.size() + "]");
        }
    }

    public int getLogCount() {
        synchronized (bufferLock) {
            return displayLogs.size() + writeBuffer.size();
        }
    }

    public boolean isLowMemoryMode() {
        return isLowMemoryMode;
    }

    public int getBufferSize() {
        synchronized (bufferLock) {
            return writeBuffer.size();
        }
    }

    private void printStatistics() {
        long now = System.currentTimeMillis();
        if (now - lastStatTime >= STAT_INTERVAL) {
            lastStatTime = now;

            int displaySize = displayLogs.size();
            int bufferSize = writeBuffer.size();
            String mode = isLowMemoryMode ? "低内存模式" : "正常模式";

            Log.i(TAG, String.format(
                    "📊 运行统计 [累计追加=%d, 累计刷新=%d, 显示=%d, 缓冲=%d, 模式=%s]",
                    totalAppendCount, totalFlushCount, displaySize, bufferSize, mode
            ));
        }
    }

    public void setDebugLogEnabled(boolean enabled) {
        this.enableDebugLog = enabled;
        Log.i(TAG, "调试日志 " + (enabled ? "已启用" : "已禁用"));
    }

    public void destroy() {
        isDestroyed = true;
        uiHandler.removeCallbacksAndMessages(null);

        Log.i(TAG, String.format(
                "LogView 销毁 [生命周期统计: 累计追加=%d, 累计刷新=%d, 最终日志数=%d]",
                totalAppendCount, totalFlushCount, displayLogs.size()
        ));

        clearLogs();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    public void setAutoScroll(boolean enable) {
        autoScroll = enable;
    }

    public void setMaxLogs(int max) {
        this.maxLogs = Math.max(100, Math.min(max, 10000));
    }

    public void setTextColor(int color) {
        this.textColor = color;
        logAdapter.setTextColor(color);
        uiHandler.post(() -> {
            if (!isDestroyed) logAdapter.notifyDataSetChanged();
        });
    }

    public void setTextSize(float sizeSp) {
        this.textSizeSp = sizeSp;
        logAdapter.setTextSize(sizeSp);
        uiHandler.post(() -> {
            if (!isDestroyed) logAdapter.notifyDataSetChanged();
        });
    }

    // ===================== Adapter =====================

    private static class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private final LinkedList<String> logList;
        private int textColor;
        private float textSizeSp;

        public LogAdapter(LinkedList<String> logList, int color, float textSizeSp) {
            this.logList = logList;
            this.textColor = color;
            this.textSizeSp = textSizeSp;
        }

        public void setTextColor(int color) { this.textColor = color; }
        public void setTextSize(float sizeSp) { this.textSizeSp = sizeSp; }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tv.setPadding(16, 8, 16, 8);
            tv.setGravity(Gravity.START);
            tv.setBackgroundColor(Color.TRANSPARENT);
            tv.setSingleLine(false);
            return new LogViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView;
            tv.setTextColor(textColor);
            tv.setTextSize(textSizeSp);

            if (position >= 0 && position < logList.size()) {
                tv.setText(logList.get(position));
            } else {
                tv.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return logList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class LogViewHolder extends RecyclerView.ViewHolder {
        public LogViewHolder(@NonNull TextView itemView) {
            super(itemView);
        }
    }
}
