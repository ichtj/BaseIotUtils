package com.face_chtj.base_iotutils.download;

public class DownloadTask {
    private String url;
    private String filePath;
    private int progress;
    private boolean isPaused;

    // Constructors, getters, and setters
    public DownloadTask(String url, String filePath, int progress, boolean isPaused) {
        this.url = url;
        this.filePath = filePath;
        this.progress = progress;
        this.isPaused = isPaused;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
}

