package com.face_chtj.base_iotutils.audio;

import com.face_chtj.base_iotutils.enums.PLAY_STATUS;

/**
 * 播放进度，状态监听
 */
public interface PlayStateChangeListener {
    void onPlayStateChange(PLAY_STATUS play_status);

    void getProgress(int sumProgress, int nowProgress);//返回播放进度
}
