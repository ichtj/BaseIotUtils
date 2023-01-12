package com.face_chtj.base_iotutils;

import android.media.MediaPlayer;

import com.face_chtj.base_iotutils.callback.IAudioState;
import com.face_chtj.base_iotutils.callback.IPlayStateCallback;
import com.face_chtj.base_iotutils.callback.IVolumeType;

/**
 * 音频播放
 * {@link #startPlaying(String)} 开始播放
 * {@link #startPlaying(String, int)} 开始播放 ,VOLUME_TYPE声道类型
 * {@link #setVolumeType(int)} 切换声道
 * {@link #resumePlay()} 继续播放
 * {@link #pausePlay()} 暂停播放
 * {@link #stopPlaying()} 继续播放
 */
public class AudioUtils {
    private static final String TAG = "PlayUtils";
    private IPlayStateCallback IPlayStateCallback;
    private MediaPlayer player;
    private int mDuration = 0;//总时长
    private int nowDuration = 0;//当前时长
    private static AudioUtils audioUtils;

    public static final int PLAY=0x100;
    public static final int RESUME=0x101;
    public static final int PAUSE=0x102;
    public static final int STOP=0x103;
    public static final int NONE=0x104;
    public static final int LEFT=0x201;
    public static final int RIGHT=0x202;
    public static final int ALL=0x203;

    //是否开始计算进度
    private boolean isCalculationProgress = false;
    //private PLAY_STATUS play_status = PLAY_STATUS.NONE;
    private @IAudioState int nowState=NONE;

    //单例模式
    public static AudioUtils getInstance() {
        if (audioUtils == null) {
            synchronized (BaseIotUtils.class) {
                if (audioUtils == null) {
                    audioUtils = new AudioUtils();
                }
            }
        }
        return audioUtils;
    }

    public AudioUtils setPlayStateChangeListener(IPlayStateCallback listener) {
        this.IPlayStateCallback = listener;
        return this;
    }

    /**
     * 开始播放
     *
     * @param filePath 文件路径
     * @return
     */
    public AudioUtils startPlaying(String filePath) {
        return startPlaying(filePath, ALL);
    }

    /**
     * 开始播放
     *
     * @param filePath    文件路径
     * @param volume_type 声道类型
     * @return
     */
    public AudioUtils startPlaying(String filePath, @IVolumeType final int volume_type) {
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
                        if (volume_type == LEFT) {
                            setVolumeType(LEFT);
                        } else if (volume_type == RIGHT) {
                            setVolumeType(RIGHT);
                        } else {
                            setVolumeType(ALL);
                        }
                        audioUtils.player.start();
                        if (IPlayStateCallback != null) {
                            startCalculationProgress();
                            if (IPlayStateCallback != null) {
                                nowState = PLAY;
                                IPlayStateCallback.onPlayStateChange(nowState);
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
        return audioUtils;
    }

    /**
     * 切换声道
     */
    public void setVolumeType(@IVolumeType int volume_type) {
        if (audioUtils != null && audioUtils.player != null) {
            if (volume_type == LEFT) {
                audioUtils.player.setVolume(1, 0);
            } else if (volume_type == RIGHT) {
                audioUtils.player.setVolume(0, 1);
            } else {
                audioUtils.player.setVolume(1, 1);
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        try {
            if (audioUtils.player != null) {
                audioUtils.player.pause();
                //赋值到当前进度上
                nowDuration = audioUtils.player.getCurrentPosition();
                if (audioUtils.IPlayStateCallback != null) {
                    nowState = PAUSE;
                    audioUtils.IPlayStateCallback.onPlayStateChange(nowState);
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
                nowState = RESUME;
                audioUtils.IPlayStateCallback.onPlayStateChange(nowState);
                isCalculationProgress = true;
                audioUtils.player.seekTo(nowDuration);
                audioUtils.player.start();
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
                            if (nowState == PAUSE || nowState == STOP) {
                                //如果此时播放状态为暂停或者停止 那么需要中断这个任务
                                isCalculationProgress = false;
                                break;
                            }
                            //获取当前播放的进度
                            int currentposition = player.getCurrentPosition();
                            KLog.d(TAG, "currentposition>>>" + currentposition);
                            nowDuration = currentposition;
                            IPlayStateCallback.getProgress(mDuration, nowDuration);
                            boolean isPlayings = isPlaying();
                            KLog.d(TAG, " isPlayings= " + isPlayings + ",nowDuration=" + nowDuration + "mDuration=" + mDuration);
                            if (isPlayings == false) {
                                //播放已经完成
                                KLog.d(TAG, ".....0");
                                isCalculationProgress = false;
                                //当前的时长 大于总的时长
                                IPlayStateCallback.getProgress(mDuration, mDuration);
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
            if (audioUtils.player != null) {
                audioUtils.player.stop();
                audioUtils.player.reset();

                if (audioUtils.IPlayStateCallback != null) {
                    nowState = STOP;
                    audioUtils.IPlayStateCallback.onPlayStateChange(nowState);
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
            return audioUtils.player != null && audioUtils.player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }
}
