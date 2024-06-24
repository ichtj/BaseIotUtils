package com.face_chtj.base_iotutils.download;

public class DownloadStatus {
    public enum Status {
        STARTED,
        PAUSED,
        RESUMED,
        COMPLETED,
        ERROR
    }

    private String url;
    private Status status;
    private String errorMessage;

    public DownloadStatus(String url, Status status) {
        this.url = url;
        this.status = status;
    }

    public DownloadStatus(String url, Status status, String errorMessage) {
        this.url = url;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public String getUrl() {
        return url;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "DownloadStatus{" +
                "url='" + url + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
