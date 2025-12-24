package com.face_chtj.base_iotutils.download;

public interface DownloadCallback {
    void onDownloadStatusChanged(DownloadStatus status);
    void onDownloadProgress(String url, int progress);
}
