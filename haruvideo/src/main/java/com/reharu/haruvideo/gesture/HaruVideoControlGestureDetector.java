package com.reharu.haruvideo.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.reharu.haruvideo.control.HaruVideoController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoshino on 2018/6/25.
 */

public class HaruVideoControlGestureDetector extends HaruVideoControlGestureRecognizer {

    private List<HaruVideoControlListener> listenerList = new ArrayList<>();

    private GestureDetector gestureDetector;

    private HaruVideoController controller;


    public HaruVideoControlGestureDetector(Context context, HaruVideoController controller) {
        super(context, controller);

        gestureDetector = new GestureDetector(context, this);

        this.controller = controller;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean flag = gestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                flag = this.onUp(ev) && flag;
                break;
        }
        return flag;
    }

    /**
     * 子View的触摸事件
     */
    public void onChildTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                this.onUp(ev, true);
                break;
            case MotionEvent.ACTION_DOWN:
                this.onDown(ev, true);
                break;
        }
    }

    public void addHaruVideoControlListener(HaruVideoControlListener listener) {
        if (listener != null) {
            listenerList.add(listener);
        }
    }


    @Override
    protected void onShowControlPanel() {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onShowControlPanel();
        }
    }

    @Override
    protected void onHideControlPanel() {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onHideControlPanel();
        }
    }

    @Override
    protected void onToggleControlPanel() {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onToggleControlPanel();
        }
    }

    @Override
    protected void onStartSetBrightness() {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onStartSetBrightness();
        }
    }

    @Override
    protected void onStartSetVolume() {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onStartSetVolume();
        }
    }

    @Override
    protected void onAddBrightness(float lPercent) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onAddBrightness(lPercent);
        }
    }

    @Override
    protected void onAddProgress(float pPercent) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onAddProgress(pPercent);
        }
    }

    @Override
    protected void onAddVolume(float vPercent) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onAddVolume(vPercent);
        }
    }

    @Override
    protected void onSubmitVolume(float vPercent) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onSubmitVolume(vPercent);
        }
    }

    @Override
    protected void onSubmitProgress(float pDuration) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onSubmitProgress(pDuration);
        }
    }

    @Override
    protected void onSubmitBrightness(float lPercent) {
        for (HaruVideoControlListener listener : listenerList) {
            listener.onSubmitBrightness(lPercent);
        }
    }
}
