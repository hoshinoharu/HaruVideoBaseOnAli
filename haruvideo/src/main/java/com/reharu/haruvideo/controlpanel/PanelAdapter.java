package com.reharu.haruvideo.controlpanel;

import android.view.ViewGroup;

/**
 * Created by hoshino on 2018/6/26.
 */

public interface PanelAdapter {

    void onInitTopPanel(ViewGroup topPanel);

    void onInitBottomPanel(ViewGroup bottomPanel);

    void onInitLeftPanel(ViewGroup leftPanel);

    void onInitRightPanel(ViewGroup rightPanel);

    void onInitFrontPanel(ViewGroup frontPanel);
}
