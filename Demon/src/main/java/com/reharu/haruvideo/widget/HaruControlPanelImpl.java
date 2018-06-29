package com.reharu.haruvideo.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.reharu.haruvideo.R;
import com.reharu.haruvideo.control.HaruVideoController;
import com.reharu.haruvideo.controlpanel.HaruBaseControlPanel;
import com.reharu.haruvideo.controlpanel.PanelAdapter;
import com.reharu.haruvideo.gesture.HaruVideoControlListener;
import com.reharu.haruvideo.utils.TimeUtil;
import com.reharu.haruvideo.view.HaruSeekBar;

/**
 * Created by hoshino on 2018/6/21.
 */

public class HaruControlPanelImpl extends HaruBaseControlPanel implements Handler.Callback, HaruSeekBar.OnProgressSubmitListener, PanelAdapter {
    /**
     * 用于计时的handler
     */
    private Handler handler = new Handler(this);


    /**
     * 全屏面板
     */
    private ViewGroup fullBottomPanel;


    /**
     * 正常面板
     */
    private ViewGroup normalBottomPanel;

    /**
     * 隐藏的延迟
     */
    private long hideDelay = 5 * 1000;

    //对外共用的播放按钮
    private ImageView playButton;

    //对外共用的进度条
    private HaruSeekBar seekBar;

    //显示时间的textView
    private TextView timeText;

    //正常的播放按钮
    private ImageView normalPlayButton;

    //正常的进度条
    private HaruSeekBar normalSeekBar;

    //全屏的播放按钮
    private ImageView fullPlayButton;

    //全屏的进度条
    private HaruSeekBar fullSeekBar;

    private TextView normalScreenTime;

    private TextView fullScreenTime;

    //返回按钮
    private ImageView backBtn;

    private ImageView fullScreenButton;

    private View loadingView;

    private ImageView voiceBrightImg;

    private SeekBar voiceBrightSeekBar;

    private ViewGroup voiceBrightPanel;

    public HaruControlPanelImpl(@NonNull Context context) {
        super(context);
        addPanelAdapter(this);
    }

    public HaruControlPanelImpl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addPanelAdapter(this);
    }

    @Override
    public void onPlay() {
        if (controller.canControlProgress()) {
            playButton.setImageResource(getPauseImageResID());
        } else {
            playButton.setImageResource(getStopImageResID());
        }
    }

    protected int getStopImageResID() {
        return R.drawable.hr_v_stop;
    }

    protected int getPauseImageResID() {
        return R.drawable.hr_v_pause;
    }

    protected int getPlayImageResID() {
        return R.drawable.hr_v_play;
    }

    @Override
    public void onPause() {
        playButton.setImageResource(getPlayImageResID());
    }

    @Override
    public void onStop() {
        playButton.setImageResource(getPlayImageResID());
    }

    @Override
    public void onHide() {
        super.onHide();
        cancelDelayHide();
    }

    @Override
    public void onInitTopPanel(ViewGroup topPanel) {
        View view = LayoutInflater.from(topPanel.getContext()).inflate(R.layout.hr_v_top_panel, topPanel, true);
        backBtn = view.findViewById(R.id.backBtn);
    }

    @Override
    public void onInitBottomPanel(ViewGroup bottomPanel) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.hr_v_bottom_panel, bottomPanel, true);

        fullScreenButton = view.findViewById(R.id.fullScreenButton);
        fullScreenButton.setOnClickListener(v -> controller.fullScreen((Activity) getContext()));

        fullBottomPanel = view.findViewById(R.id.fullScreenTypeBottomPanel);
        normalBottomPanel = view.findViewById(R.id.normalScreenTypeBottomPanel);

        normalPlayButton = view.findViewById(R.id.normalScreenPlayButton);
        normalSeekBar = view.findViewById(R.id.normalScreenSeekBar);
        fullSeekBar = view.findViewById(R.id.fullScreenSeekBar);
        fullPlayButton = view.findViewById(R.id.fullScreenPlayButton);
        normalScreenTime = view.findViewById(R.id.normalScreenTime);
        fullScreenTime = view.findViewById(R.id.fullScreenTime);

    }

    @Override
    public void onInitLeftPanel(ViewGroup leftPanel) {

    }

    @Override
    public void onInitRightPanel(ViewGroup rightPanel) {

    }

    @Override
    public void onInitFrontPanel(ViewGroup frontPanel) {
        View view = LayoutInflater.from(frontPanel.getContext()).inflate(R.layout.hr_v_front_panel, frontPanel, true);
        loadingView = view.findViewById(R.id.loadingView);
        voiceBrightImg = view.findViewById(R.id.voiceBrightImg);
        voiceBrightSeekBar = view.findViewById(R.id.voiceBrightSeekBar);
        voiceBrightPanel = view.findViewById(R.id.voiceBrightPanel);
    }

    @Override
    public void onBindController(HaruVideoController controller) {
        super.onBindController(controller);
        onShow();
        delayHide();
        addHaruVideoControlListener(new HaruVideoControlListener() {

            private ObjectAnimator hideAnimator;

            @Override
            public void onShowControlPanel() {
                cancelDelayHide();
                onShow();
            }

            @Override
            public void onHideControlPanel() {
                delayHide();
            }

            @Override
            public void onToggleControlPanel() {
                toggle();
            }

            @Override
            public void onAddBrightness(float lPercent) {
                float bP = controller.getBrightness() + lPercent;
                controller.setBrightness(bP);
                voiceBrightSeekBar.setProgress((int) controller.getBrightness());
            }

            @Override
            public void onAddProgress(float pPercent) {
                if (controller.canControlProgress()) {
                    seekBar.addProgressPercent(pPercent);
                    int videoDuration = controller.getVideoDuration();
                    //计算出要设置的播放位置
                    int playPos = (int) (videoDuration * seekBar.getCurProgressPercent() / 100);
                    //改变播放位置
                    onPlayPositionChange(playPos, videoDuration);
                }
            }

            @Override
            public void onAddVolume(float vPercent) {
                float vp = vPercent + controller.getVolume();
                controller.setVolume(vp);
                voiceBrightSeekBar.setProgress((int) controller.getVolume());
            }

            @Override
            public void onSubmitVolume(float vPercent) {
                initAnimator();
                hideAnimator.start();
            }

            @Override
            public void onSubmitProgress(float pDuration) {
                if (controller.canControlProgress()) {
                    seekBar.submitProgress();
                }
            }

            @Override
            public void onSubmitBrightness(float lPercent) {
                initAnimator();
                hideAnimator.start();
            }

            private void initAnimator() {
                if (hideAnimator == null) {
                    hideAnimator = ObjectAnimator.ofFloat(voiceBrightPanel, "alpha", 1, 0);
                    hideAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            voiceBrightPanel.setVisibility(GONE);
                            voiceBrightPanel.setAlpha(1);
                        }
                    });
                    hideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Log.e("TAG", valueAnimator.getAnimatedValue() + "");
                        }
                    });
                    hideAnimator.setDuration(500);
                }
                hideAnimator.setIntValues(1, 0);
            }

            @Override
            public void onStartSetBrightness() {
                cancelHide();
                voiceBrightPanel.setVisibility(VISIBLE);
                voiceBrightImg.setImageResource(R.drawable.hr_v_brightness);
                voiceBrightSeekBar.setProgress((int) controller.getBrightness());
            }

            @Override
            public void onStartSetVolume() {
                cancelHide();
                voiceBrightPanel.setVisibility(VISIBLE);
                voiceBrightImg.setImageResource(R.drawable.hr_v_voice);
                voiceBrightSeekBar.setProgress((int) controller.getVolume());

            }

            private void cancelHide() {
                if (hideAnimator != null && hideAnimator.isRunning()) {
                    hideAnimator.cancel();
                    voiceBrightPanel.setAlpha(1);
                }
            }
        });
    }

    @Override
    public void onPlayPositionChange(int playPos, int duration) {
        if (seekBar != null && !seekBar.isOnChangingProgress()) {
            seekBar.setProgressPercent(playPos * 1.0f / duration * 100);
        }
        String s = TimeUtil.formatLongTimeTommss(playPos) + (duration < 0 ? "" : ("/" + TimeUtil.formatLongTimeTommss(duration)));
        timeText.setText(s);
    }

    @Override
    public void onBufferPositionChange(int bufferPosition, int videoDuration) {
        if (seekBar != null && !seekBar.isOnChangingProgress()) {
            seekBar.setSecondaryProgressPercent(bufferPosition * 1.0f / videoDuration * 100);
        }
    }

    @Override
    public void onStartBufferLoading() {
        loadingView.setVisibility(VISIBLE);
        Log.e("TAG", "开始加载");
    }

    @Override
    public void onEndBufferLoading() {
        loadingView.setVisibility(GONE);
        Log.e("TAG", "结束加载");
    }

    @Override
    public void onFullScreenSize() {
        if (playButton != null) {
            fullPlayButton.setImageDrawable(playButton.getDrawable());
        }
        if (timeText != null) {
            fullScreenTime.setText(timeText.getText());
        }

        playButton = fullPlayButton;

        playButton.setOnClickListener(v -> controller.toggle());
        timeText = fullScreenTime;

        normalBottomPanel.setVisibility(GONE);
        fullBottomPanel.setVisibility(VISIBLE);

        if (controller.canControlProgress()) {
            fullSeekBar.setVisibility(VISIBLE);
            if (seekBar != null) {
                seekBar.setOnProgressSubmitListener(null);
                fullSeekBar.setProgressPercent(seekBar.getCurProgressPercent());
                fullSeekBar.setSecondaryProgressPercent(seekBar.getCurSecondaryProgressPercent());
            }
            seekBar = fullSeekBar;
            seekBar.setOnProgressSubmitListener(this);
        } else {
            fullSeekBar.setVisibility(GONE);
        }
        super.onFullScreenSize();
    }

    @Override
    public void onNormalSize() {
        if (playButton != null) {
            normalPlayButton.setImageDrawable(playButton.getDrawable());
        }
        if (timeText != null) {
            normalScreenTime.setText(timeText.getText());
        }
        playButton = normalPlayButton;
        playButton.setOnClickListener(v -> controller.toggle());
        timeText = normalScreenTime;

        fullBottomPanel.setVisibility(GONE);
        normalBottomPanel.setVisibility(VISIBLE);
        if (controller.canControlProgress()) {
            normalSeekBar.setVisibility(VISIBLE);
            if (seekBar != null) {
                seekBar.setOnProgressSubmitListener(null);
                normalSeekBar.setProgressPercent(seekBar.getCurProgressPercent());
                normalSeekBar.setSecondaryProgressPercent(seekBar.getCurSecondaryProgressPercent());
            }
            seekBar = normalSeekBar;
            seekBar.setOnProgressSubmitListener(this);
        } else {
            normalSeekBar.setVisibility(GONE);
        }
        if (controller.canControlProgress()) {
            normalScreenTime.setText("00:00/00:00");
            fullScreenTime.setText("00:00/00:00");
        } else {
            normalScreenTime.setText("00:00");
            fullScreenTime.setText("00:00");
        }
        super.onNormalSize();
    }

    private void cancelDelayHide() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void toggle() {
        super.toggle();
        //显示后延迟隐藏
        if (isShowing) {
            delayHide();
        }
    }

    /**
     * 延迟隐藏
     */
    private void delayHide() {
        cancelDelayHide();
        handler.sendEmptyMessageDelayed(0, hideDelay);
    }

    @Override
    public boolean handleMessage(Message message) {
        onHide();
        return true;
    }

    public ImageView getPlayButton() {
        return playButton;
    }

    public HaruSeekBar getSeekBar() {
        return seekBar;
    }

    public ImageView getFullScreenButton() {
        return fullScreenButton;
    }

    public ImageView getBackBtn() {
        return backBtn;
    }

    @Override
    public void onSubmitProgress(int progress, float percent) {
        controller.setPlayPosition((int) (controller.getVideoDuration() * percent / 100));
    }
}
