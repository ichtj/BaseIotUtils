package com.face_chtj.base_iotutils;

import android.media.MediaPlayer;

import com.face_chtj.base_iotutils.keeplive.BaseIotUtils;

/**
 * 音频播放
 * {@link #startPlaying(String)} 开始播放
 * {@link #resumePlay()} 继续播放
 * {@link #pausePlay()} 暂停播放
 * {@link #stopPlaying()} 继续播放
 */
public class PlayUtils {
    private static final String TAG = "PlayUtils";
    private PlayStateChangeListener playStateChangeListener;
    private MediaPlayer player;
    private int mDuration = 0;//总时长
    private int nowDuration = 0;//当前时长
    private static PlayUtils playUtils;
    //是否开始计算进度
    private boolean isCalculationProgress = false;
    private PLAY_STATUS play_status = PLAY_STATUS.NONE;

    public enum PLAY_STATUS {
        PLAY, RESUME, PAUSE, STOP, NONE
    }

    //单例模式
    public static PlayUtils getInstance() {
        if (playUtils == null) {
            synchronized (BaseIotUtils.class) {
                if (playUtils == null) {
                    playUtils = new PlayUtils();
                }
            }
        }
        return playUtils;
    }

    public interface PlayStateChangeListener {
        void onPlayStateChange(PLAY_STATUS play_status);

        void getProgress(int sumProgress, int nowProgress);//返回播放进度
    }

    public PlayUtils setPlayStateChangeListener(PlayStateChangeListener listener) {
        this.playStateChangeListener = listener;
        return this;
    }

    /**
     * 开始播放
     */
    public PlayUtils startPlaying(String filePath) {
        if (isPlaying()) {
            KLog.d(TAG, " = 正在播放中");
        } else {
            try {
                //未播放时执行播放
                isCalculationProgress = true;
                player = new MediaPlayer();
                player.setDataSource(filePath);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) { //返回音频时长
                        playUtils.player.start();
                        if (playStateChangeListener != null) {
                            startCalculationProgress();
                            if (playStateChangeListener != null) {
                                play_status = PLAY_STATUS.PLAY;
                                playStateChangeListener.onPlayStateChange(play_status);
                            }
                        }
                    }
                });

                // play over call back
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        nowDuration = mDuration;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return playUtils;
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        try {
            if (playUtils.player != null) {
                playUtils.player.pause();
                //赋值到当前进度上
                nowDuration = playUtils.player.getCurrentPosition();
                if (playUtils.playStateChangeListener != null) {
                    play_status = PLAY_STATUS.PAUSE;
                    playUtils.playStateChangeListener.onPlayStateChange(play_status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 继续播放
     */
    public void resumePlay() {
        KLog.d(TAG, " nowDuration= " + nowDuration);
        if (nowDuration > 0) {
            try {
                play_status = PLAY_STATUS.RESUME;
                playUtils.playStateChangeListener.onPlayStateChange(play_status);
                isCalculationProgress = true;
                playUtils.player.seekTo(nowDuration);
                playUtils.player.start();
                startCalculationProgress();

            } catch (Exception e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
            }

        }
    }

    /***
     * 开始计算进度
     */
    public void startCalculationProgress() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    int duration = player.getDuration();
                    if (duration <= 0) {
                        //返回时长错误
                        duration = -1;
                    }
                    mDuration = duration;
                    while (isCalculationProgress) {
                        if (mDuration > 0) {
                            if (play_status == PLAY_STATUS.PAUSE || play_status == PLAY_STATUS.STOP) {
                                //如果此时播放状态为暂停或者停止 那么需要中断这个任务
                                isCalculationProgress = false;
                                break;
                            }
                            //获取当前播放的进度
                            int currentposition = player.getCurrentPosition();
                            KLog.d(TAG, "currentposition>>>" + currentposition);
                            nowDuration = currentposition;
                            playStateChangeListener.getProgress(mDuration, nowDuration);
                            boolean isPlayings = isPlaying();
                            KLog.d(TAG, " isPlayings= " + isPlayings + ",nowDuration=" + nowDuration + "mDuration=" + mDuration);
                            if (isPlayings == false) {
                                //播放已经完成
                                KLog.d(TAG, ".....0");
                                isCalculationProgress = false;
                                //当前的时长 大于总的时长
                                playStateChangeListener.getProgress(mDuration, mDuration);
                                stopPlaying();
                                break;
                            } else {
                                //播放完毕后该任务需要中断 位播放完毕 则继续执行任务获取进度
                                Thread.sleep(1000);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    KLog.e(TAG, "errMeg:" + e.getMessage());
                }
            }
        }.start();

    }

    /**
     * 停止播放
     */
    public void stopPlaying() {
        try {
            if (playUtils.player != null) {
                playUtils.player.stop();
                playUtils.player.reset();

                if (playUtils.playStateChangeListener != null) {
                    play_status = PLAY_STATUS.STOP;
                    playUtils.playStateChangeListener.onPlayStateChange(play_status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        try {
            return playUtils.player != null && playUtils.player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }
}
