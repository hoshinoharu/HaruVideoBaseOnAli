package com.reharu.haruvideo.control;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.reharu.haruvideo.controlpanel.HaruControlPanel;

/**
 * Created by hoshino on 2018/6/21.
 * 视频控制器
 */

public interface HaruVideoController {

    interface OnPreparedListener {
        void onPrepared();
    }

    interface OnCompletedListener {
        void onCompleted();
    }

    interface OnErrorListener {
        void onError(int errorCode, String msg);
    }

    enum VideoState {
        PREPARING,
        PREPARED,
        PREPARE_LOADING,//预加载状态 真正加载之前会有 2次预加载调用
        LOADING,
        PLAY,
        PAUSE,
        ERROR, STOP
    }


    /**
     * 设置播放url
     *
     * @param url 播放地址
     */
    void setPlayUrl(String url);

    /**
     * 设置监听回调
     *
     * @param listener
     */
    void setOnVideoPreparedListener(OnPreparedListener listener);

    void play();

    void pause();

    void stop();

    /**
     * 如果在播放就暂停 如果暂停就播放
     */
    void toggle();

    /**
     * 暂停后开始播放 主要和activity生命周期绑定
     */
    void resumePlay();

    /**
     * 重新从头开始播放
     */
    void replay();

    /**
     * 释放资源
     */
    void destroy();

    /**
     * 旋转 阿里播放器 只支持 0 90 180 270 度
     */
    void rotate(int rotate);

    /**
     * 绑定控制面板
     */
    void bindControlPanel(HaruControlPanel panel);

    /**
     * 已经缓存的视频位置
     *
     * @return
     */
    int getBufferPosition();

    /**
     * 当前播放的视频位置
     *
     * @return
     */
    int getCurVideoPosition();

    /**
     * 当前视频长度
     *
     * @return
     */
    int getVideoDuration();

    /**
     * 设置播放位置
     */
    void setPlayPosition(int playPosition);


    /**
     * 设置缓冲区大小
     */
    void setBufferSize(long size);

    /**
     * 视频是否可以控制进度
     *
     * @return 比如直播就无法控制进度
     */
    boolean canControlProgress();

    void setOnCompletedListener(OnCompletedListener listener);

    void setOnErrorListener(OnErrorListener listener);

    /**
     * 设置亮度
     */
    void setBrightness(float lPercent);

    /**
     * 获取亮度
     */
    float getBrightness();

    /**
     * 设置音量
     */
    void setVolume(float vPercent);

    /**
     * 获取音量
     */
    float getVolume();

    /**
     * 进入全屏
     */
    void fullScreen(Activity activity);

    /**
     * 正常大小
     */
    void normalSize();

    /**
     * 开关全屏
     */
    void toggleFullScreen(Activity activity);


    /**
     * 是否全屏
     */
    boolean isFullScreen() ;

    /**
     * 检查权限
     * 缓存需要 文件读写权限
     */
    void checkPermission(Activity activity) ;

    /**
     * 设置播放器refer //http://reharu.com
     */
    void setPlayRefer(String refer) ;

    /**
     * 获取 视频播放展示尺寸
     */
    int getVideoViewWidth() ;

    int getVideoViewHeight() ;

}
