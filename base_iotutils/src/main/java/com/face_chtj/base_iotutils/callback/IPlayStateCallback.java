package com.face_chtj.base_iotutils.callback;

/**
 * 播放进度，状态监听
 */
public interface IPlayStateCallback {
    void onPlayStateChange(@IAudioState int play_status);

    void getProgress(int sumProgress, int nowProgress);//返回播放进度
}
