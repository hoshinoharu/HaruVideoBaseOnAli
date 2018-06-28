package com.reharu.haruvideo.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

/**
 * Created by hoshino on 2018/6/25.
 */

public class HaruSeekBar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {

    /**
     * 正在改变进度
     */
    private boolean isOnChangingProgress = false;

    //提交进度的回调
    public interface OnProgressSubmitListener {
        void onSubmitProgress(int progress, float percent);
    }

    private float progressPercent = 0;

    private OnProgressSubmitListener submitListener;

    public HaruSeekBar(Context context) {
        super(context);
        super.setOnSeekBarChangeListener(this);
    }

    public HaruSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(this);
    }

    public HaruSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnSeekBarChangeListener(this);
    }

    public void setOnProgressSubmitListener(OnProgressSubmitListener submitListener) {
        this.submitListener = submitListener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isOnChangingProgress = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        progressPercent = getCurProgressPercent();
        submitProgress();
    }

    public void submitProgress() {
        isOnChangingProgress = false;
        this.submitListener.onSubmitProgress(this.getProgress(), progressPercent);
    }


    public void addProgressPercent(float percent) {
        isOnChangingProgress = true;
        this.setProgressPercent(this.progressPercent + percent);
    }

    public void setProgressPercent(float percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        //获取进度百分比
        this.progressPercent = percent;

        int targetProgress = (int) (this.getMax() * percent / 100);
        this.setProgress(targetProgress);
    }



    //设置第二进度百分比
    public void setSecondaryProgressPercent(float percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        int targetProgress = (int) (this.getMax() * percent / 100);
        this.setSecondaryProgress(targetProgress);
    }

    public float getCurProgressPercent() {
        return this.getProgress() * 1.0f / this.getMax() * 100;
    }

    public float getCurSecondaryProgressPercent() {
        return this.getSecondaryProgress() * 1.0f / this.getMax() * 100;
    }

    public boolean isOnChangingProgress() {
        return isOnChangingProgress;
    }
}
