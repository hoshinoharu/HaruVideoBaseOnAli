package com.reharu.haruvideo.gesture;

/**
 * Created by hoshino on 2018/6/26.
 */

public interface HaruVideoControlListener {

    void onShowControlPanel();

    void onHideControlPanel();

    /**
     * 开关面板
     */
    void onToggleControlPanel();

    void onAddBrightness(float lPercent);

    void onAddProgress(float pPercent);

    void onAddVolume(float vPercent);

    /**
     * 提交音量修改
     */
    void onSubmitVolume(float vPercent);

    /**
     * 提交进度修改
     */
    void onSubmitProgress(float pDuration);

    /**
     * 提交亮度修改
     */
    void onSubmitBrightness(float lPercent);

    /**
     * 开始设置亮度
     */
    void onStartSetBrightness();

    /**
     * 开始设置声音
     */
    void onStartSetVolume();
}
