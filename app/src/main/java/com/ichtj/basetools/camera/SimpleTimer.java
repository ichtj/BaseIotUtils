package com.ichtj.basetools.camera;

public class SimpleTimer {

    private volatile boolean running = false;
    private Thread thread;

    public interface Callback {
        void onTick();
    }

    public void start(long intervalMs, Callback callback) {
        if (running) return;

        running = true;

        thread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    break;
                }

                if (running && callback != null) {
                    callback.onTick();
                }
            }
        });

        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}