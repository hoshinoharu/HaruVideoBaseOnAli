package com.reharu.haruvideo.gesture;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.reharu.haruvideo.control.HaruVideoController;

/**
 * Created by hoshino on 2018/6/25.
 */

public abstract class HaruVideoControlGestureRecognizer implements GestureDetector.OnGestureListener {

    enum ControlState {
        WAITING,//等待用户操作
        SET_PROGRESS,//设置进度
        SET_VOLUME,//设置音量
        SET_LIGHTNESS,//设置亮度
    }

    private Context context;
    private ControlState controlState;

    /**
     * 缓存触摸坐标
     */
    private float preX;//上一次触摸x坐标
    private float preY;//上一次触摸y坐标
    private float firstX;//第一次触摸x坐标
    private float firstY;//第一次触摸y坐标

    private float volume = 0;
    private float lightness = 0;
    private float progress = 0;

    private boolean needHide = true;

    private HaruVideoController controller;

    private int videoWidth;

    private int videoHeight;

    public HaruVideoControlGestureRecognizer(Context context, HaruVideoController controller) {
        this.context = context;
        this.controlState = ControlState.WAITING;
        this.controller = controller;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return onDown(motionEvent, false);
    }

    /**
     * 每次按下时调用
     * 手势识别的开始
     *
     * @param fromChild 是否是子view传来的事件
     */
    public boolean onDown(MotionEvent motionEvent, boolean fromChild) {
        if (!fromChild) {
            controlState = ControlState.WAITING;
            //触摸开始时获取视频尺寸信息
            videoWidth = controller.getVideoViewWidth();
            videoHeight = controller.getVideoViewHeight();
            firstX = motionEvent.getRawX();
            firstY = motionEvent.getRawY();
            preX = firstX;
            preY = firstY;
            volume = 0;
            progress = 0;
            lightness = 0;
            needHide = true;
        } else {
            onShowControlPanel();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    /**
     * 单击时调用
     */
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        onToggleControlPanel();
        return true;
    }

    /**
     * 手指在屏幕上移动时调用
     *
     * @param v  水平速度
     * @param v1 垂直速度
     */
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        //这里的回调尽量不要做耗时操作，可以用来更新UI
        switch (controlState) {
            case WAITING:
                if (Math.abs(v) > Math.abs(v1)) {
                    //水平移动设置播放进度
                    controlState = ControlState.SET_PROGRESS;
                } else {
                    //左半边屏幕 设置亮度
                    if (motionEvent.getRawX() <= videoWidth / 2) {
                        controlState = ControlState.SET_LIGHTNESS;
                        onStartSetBrightness();
                    } else {
                        //右半边屏幕 设置声音
                        controlState = ControlState.SET_VOLUME;
                        onStartSetVolume();
                    }
                }
                onShowControlPanel();
                break;
            case SET_VOLUME:
                float dvy = motionEvent1.getRawY() - preY;
                float vPercent = -dvy / (videoHeight / 2) * 100;
                volume += vPercent;
                //设置音量百分比
                onAddVolume(vPercent);
                break;
            case SET_PROGRESS:
                float pdx = motionEvent1.getRawX() - preX;
                //屏幕从左滑到右 视频最多快进 100秒
                float pPercent = (pdx / videoWidth) * 100;
                progress += pPercent;
                //设置视频快进时长
                onAddProgress(pPercent);
                break;
            case SET_LIGHTNESS:
                float dly = motionEvent1.getRawY() - preY;
                float lPercent = -dly / (videoHeight / 2) * 100;
                lightness += lPercent;
                //设置亮度百分比
                onAddBrightness(lPercent);
                break;
        }
        preY = motionEvent1.getRawY();
        preX = motionEvent1.getRawX();
        return true;
    }


    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    /**
     * 快速滑动离开屏幕时调用
     */
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }


    public boolean onUp(MotionEvent motionEvent) {
        return onUp(motionEvent, false);
    }

    /**
     * 抬起时调用
     * 只要接受了事件 无论如何都会调用 最后调用
     * 手势识别的结束
     *
     * @param fromChild 是否是从子View传来的事件
     */
    public boolean onUp(MotionEvent motionEvent, boolean fromChild) {
        if (!fromChild) {
            //这里做一些耗时的提交操作
            switch (controlState) {
                case SET_VOLUME:
                    onSubmitVolume(volume);
                    break;
                case SET_PROGRESS:
                    onSubmitProgress(progress);
                    break;
                case SET_LIGHTNESS:
                    onSubmitBrightness(lightness);
                    break;
            }
        }
        onHideControlPanel();
        return true;
    }

    protected abstract void onShowControlPanel();

    protected abstract void onHideControlPanel();

    protected abstract void onStartSetBrightness();

    protected abstract void onStartSetVolume();


    /**
     * 开关面板
     */
    protected abstract void onToggleControlPanel();

    protected abstract void onAddBrightness(float lPercent);

    protected abstract void onAddProgress(float pPercent);

    protected abstract void onAddVolume(float vPercent);

    /**
     * 提交音量修改
     */
    protected abstract void onSubmitVolume(float vPercent);

    /**
     * 提交进度修改
     */
    protected abstract void onSubmitProgress(float pDuration);

    /**
     * 提交亮度修改
     */
    protected abstract void onSubmitBrightness(float lPercent);

}
