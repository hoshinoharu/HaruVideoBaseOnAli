package com.reharu.haruvideo.controlpanel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.reharu.haruvideo.R;
import com.reharu.haruvideo.control.HaruVideoController;
import com.reharu.haruvideo.gesture.HaruVideoControlGestureDetector;
import com.reharu.haruvideo.gesture.HaruVideoControlGestureRecognizer;
import com.reharu.haruvideo.gesture.HaruVideoControlListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoshino on 2018/6/21.
 * 基础的控制面板 由 6块区域组成 分为两层 前景图层 背景图层 背景图层包含4块，上下左右区域
 */

public abstract class HaruBaseControlPanel extends FrameLayout implements HaruControlPanel {

    private ViewGroup leftPanel;

    private ViewGroup rightPanel;

    private ViewGroup topPanel;

    private ViewGroup bottomPanel;

    //背景面板
    private ViewGroup backgroundPanel;

    //前景面板 用作显示提示的面板
    private ViewGroup frontPanel;

    protected HaruVideoController controller;

    protected boolean isShowing = true;

    private List<PanelAdapter> panelAdapterList = new ArrayList<>();

    private List<HaruVideoControlGestureRecognizer> videoControlGestureListenerList = new ArrayList<>();

    private List<PanelStateListener> panelStateListenerList = new ArrayList<>();

    /**
     * 手势识别
     */
    private HaruVideoControlGestureDetector gestureDetector;

    public HaruBaseControlPanel(@NonNull Context context) {
        super(context);
    }

    public HaruBaseControlPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //绑定控制器的时候初始化
    @Override
    public void onBindController(HaruVideoController controller) {
        this.controller = controller;
        initView();
    }

    protected void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ctrol_panel, this, false);
        backgroundPanel = (ViewGroup) view;
        leftPanel = view.findViewById(R.id.leftPanel);
        rightPanel = view.findViewById(R.id.rightPanel);
        topPanel = view.findViewById(R.id.topPanel);
        bottomPanel = view.findViewById(R.id.bottomPanel);
        frontPanel = new FrameLayout(getContext());

        this.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(frontPanel, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //初始化面板区域
        for (PanelAdapter panelAdapter : panelAdapterList) {
            panelAdapter.onInitLeftPanel(leftPanel);
            panelAdapter.onInitRightPanel(rightPanel);
            panelAdapter.onInitTopPanel(topPanel);
            panelAdapter.onInitBottomPanel(bottomPanel);

            panelAdapter.onInitFrontPanel(frontPanel);
        }

        //初始化过后就清除
        panelAdapterList.clear();

        //添加视频手势识别
        gestureDetector = new HaruVideoControlGestureDetector(getContext(), controller);
    }


    @Override
    public void addHaruVideoControlListener(HaruVideoControlListener listener) {
        if (gestureDetector != null) {
            gestureDetector.addHaruVideoControlListener(listener);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!super.dispatchTouchEvent(ev)) {
            return gestureDetector.onTouchEvent(ev);
        } else {
            //无响应的触摸事件 主要用来在子控件响应触摸时调用 用来控制面板是否显示
            gestureDetector.onChildTouchEvent(ev);
        }
        return true;
    }

    @Override
    public void onShow() {
        if (!isShowing) {
            isShowing = true;
            backgroundPanel.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onHide() {
        if (isShowing) {
            isShowing = false;
            backgroundPanel.setVisibility(GONE);
        }
    }

    /**
     * 取消隐藏并展现
     */
    protected void toggle() {
        if (isShowing) {
            onHide();
        } else {
            onShow();
        }
    }

    @Override
    public void addPanelAdapter(PanelAdapter panelAdapter) {
        this.panelAdapterList.add(panelAdapter);
    }

    @Override
    public View getPanelView() {
        return this;
    }


    @Override
    public void addPanelStateListener(PanelStateListener listener) {
        if (listener != null) {
            panelStateListenerList.add(listener);
        }
    }

    @Override
    public void onFullScreenSize() {
        for (PanelStateListener listener : panelStateListenerList) {
            listener.onFullScreenSize();
        }
    }

    @Override
    public void onNormalSize() {
        for (PanelStateListener listener : panelStateListenerList) {
            listener.onNormalSize();
        }
    }
}
