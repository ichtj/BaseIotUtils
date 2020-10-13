package com.face_chtj.base_iotutils.download;

public interface DownloadCallBack {
    void download(String current_progress, String total_progress);
    void download(int progress);
}
