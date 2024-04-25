package com.face_chtj.base_iotutils.callback;

import com.face_chtj.base_iotutils.entity.FileData;

import java.util.List;
/**
 * 任务进度 状态返回
 */
public interface IDownloadCallback {
    //下载过程
    void downloadProgress(FileData fileData, int percent);

    //下载状态
    void downloadStatus(FileData fileData, int downloadStatus);

    //全部下载完毕
    void allDownloadComplete(List<FileData> fileDataList);

    //异常状态
    void error(FileData fileData,Throwable e);

    //任务存在
    void taskExist(FileData fileData);
}
