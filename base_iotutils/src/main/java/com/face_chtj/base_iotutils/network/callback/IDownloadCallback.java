package com.face_chtj.base_iotutils.network.callback;

import com.face_chtj.base_iotutils.entity.FileCacheData;

import java.util.List;
/**
 * 任务进度 状态返回
 */
public interface IDownloadCallback {
    //下载过程
    void downloadProgress(FileCacheData fileCacheData, int percent);

    //下载状态
    void downloadStatus(FileCacheData fileCacheData, int downloadStatus);

    //全部下载完毕
    void allDownloadComplete(List<FileCacheData> fileCacheDataList);

    //异常状态
    void error(Throwable e);

    //任务存在
    void taskExist(FileCacheData fileCacheData );
}
