package com.reharu.haruvideo.controlpanel;

import android.view.View;

import com.reharu.haruvideo.control.HaruVideoController;
import com.reharu.haruvideo.gesture.HaruVideoControlGestureRecognizer;
import com.reharu.haruvideo.gesture.HaruVideoControlListener;

/**
 * Created by hoshino on 2018/6/21.
 * 视频控制面板 接口
 * 控制面板一定要视频播放视图的父类 间接直接都可以 ；
 */
public interface HaruControlPanel {

    /**
     * 面板状态监听器
     */
    interface PanelStateListener {

        void onFullScreenSize();

        void onNormalSize();
    }

    void addPanelStateListener(PanelStateListener listener);

    /**
     * 展示状态的回调
     */
    void onShow();

    /**
     * 播放状态的回调
     */
    void onPlay();

    /**
     * 暂停状态的回调
     */
    void onPause();

    /**
     * 停止状态的回调
     */
    void onStop();

    /**
     * 隐藏状态的回调
     */
    void onHide();

    /**
     * 绑定到控制器的回调
     */
    void onBindController(HaruVideoController controller);

    /**
     * 视频播放位置改变的回调
     */
    void onPlayPositionChange(int playPos, int duration);

    /**
     * 缓存位置改变的回调
     */
    void onBufferPositionChange(int bufferPosition, int videoDuration);

    /**
     * 开始加载中
     */
    void onStartBufferLoading();

    /**
     * 结束加载中
     */
    void onEndBufferLoading();

    /**
     * 面板适配器
     * 用来初始化面板的各个区域
     * 在绑定控制器之前调用才有效
     */
    void addPanelAdapter(PanelAdapter adapter);

    /**
     * 添加视频手势监听器
     */
    void addHaruVideoControlListener(HaruVideoControlListener listener);

    /**
     * 进入全屏
     */
    void onFullScreenSize();

    /**
     * 退出全屏
     */
    void onNormalSize();

    View getPanelView();
}
