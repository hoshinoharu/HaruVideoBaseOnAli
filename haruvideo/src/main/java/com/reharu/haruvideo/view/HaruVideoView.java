package com.reharu.haruvideo.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.reharu.haruvideo.config.Config;
import com.reharu.haruvideo.control.HaruVideoController;
import com.reharu.haruvideo.controlpanel.HaruControlPanel;

/**
 * Created by hoshino on 2018/6/21.
 * 基于AliPlayer 封装的视频播放器
 */

public class HaruVideoView extends FrameLayout implements HaruVideoController, MediaPlayer.MediaPlayerPreparedListener, Handler.Callback, MediaPlayer.MediaPlayerPcmDataListener, MediaPlayer.MediaPlayerInfoListener, MediaPlayer.MediaPlayerCompletedListener, MediaPlayer.MediaPlayerErrorListener, MediaPlayer.MediaPlayerFrameInfoListener, MediaPlayer.MediaPlayerSeekCompleteListener {

    private AliVcMediaPlayer player;

    private SurfaceView surfaceView;

    private OnPreparedListener onPreparedListener;

    private OnCompletedListener onCompletedListener;

    private OnErrorListener onErrorListener;

    private HaruControlPanel controlPanel;

    private VideoState videoState = VideoState.PREPARING;

    private long maxBufferDuration;

    private String playUrl;

    /**
     * 当前视频的播放位置
     */
    private int curPlayPos;

    /**
     * 缓冲区监视器延迟
     */
    private long bufferMonitorDelay = 500;
    /**
     * 缓冲区监视器
     */
    private Handler playingHandler;

    private boolean isFullScreen = false;

    private boolean isSeeking;

    /**
     * 缓存保存目录
     */
    private String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getContext().getPackageName() + "/videoCache";


    public HaruVideoView(@NonNull Context context) {
        super(context);
    }

    public HaruVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HaruVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private AliVcMediaPlayer getPlayer() {
        if (player == null) {
            player = new AliVcMediaPlayer(getContext(), getSurfaceView());
            //缓存文件最大时长单位秒 和 最大大小单位MB
            //Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getContext().getPackageName() + "/videoCache"
            player.setPlayingCache(true, cacheDir, 60 * 60, 300);
            //默认设置最大缓冲视频长度为8000毫秒
            setBufferSize(8000);
            player.setPreparedListener(this);
            player.setPcmDataListener(this);
            player.setInfoListener(this);
            player.setCompletedListener(this);
            player.setErrorListener(this);
            player.setFrameInfoListener(this);
            player.setSeekCompleteListener(this);
            player.setRefer("http://reharu.com");
        }
        return player;
    }

    public SurfaceView getSurfaceView() {
        if (surfaceView == null) {
            surfaceView = new SurfaceView(getContext());
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
                    surfaceHolder.setKeepScreenOn(true);
                    if (player != null) {
                        player.setVideoSurface(surfaceHolder.getSurface());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    if (player != null) {
                        player.setSurfaceChanged();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            });
            this.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return surfaceView;
    }

    @Override
    public void setPlayUrl(String url) {
        this.playUrl = url;
        //调用停止状态
        if (controlPanel != null) {
            controlPanel.onStop();
        }
        videoState = VideoState.PREPARING;
        getPlayer().prepareToPlay(url);
    }

    @Override
    public void setOnVideoPreparedListener(OnPreparedListener listener) {
        this.onPreparedListener = listener;
    }

    @Override
    public void play() {
        //准备完成之后才播放 停止之后无法播放
        if (videoState != VideoState.PREPARING) {
            videoState = VideoState.PLAY;
            isSeeking = false;
            Config config = Config.getInstance(getContext());
            getPlayer().play();
            //获取音量配置 设置上
            getPlayer().setVolume((int) config.volume);
            //默认进去的时候是0 当正在准备的时候拉动进度条 会记录播放位置
            curPlayPos = getPlayer().getCurrentPosition();
            if (controlPanel != null) {
                controlPanel.onPlay();
            }
        }

    }

    @Override
    public void pause() {
        if (canControlProgress()) {
            if (videoState != VideoState.PREPARING) {
                videoState = VideoState.PAUSE;
                //保存播放进度
                curPlayPos = getPlayer().getCurrentPosition();
                getPlayer().pause();
                if (controlPanel != null) {
                    controlPanel.onPause();
                }
            }
        }
    }

    @Override
    public void stop() {
        videoState = VideoState.STOP;
        stopMonitorPlaying();
        getPlayer().stop();
        if (controlPanel != null) {
            controlPanel.onStop();
        }
    }

    /**
     * 判断能否控制视频进度 直播和普通视频 分开考虑
     */
    @Override
    public void toggle() {
        if (videoState != VideoState.PREPARING) {
            //如果播放视频资源 就暂停
            if (canControlProgress()) {
                if (videoState == VideoState.PAUSE) {
                    play();
                } else if (videoState == VideoState.PLAY) {
                    pause();
                } else if (videoState == VideoState.STOP) {
                    replay();
                }
            } else {
                //直播时无法暂停 只能停止 从新播放

                if (videoState == VideoState.STOP) {
                    replay();
                } else if (videoState == VideoState.PLAY) {
                    stop();
                }
            }
        }
    }

    @Override
    public void rotate(int rotate) {
        int ro = (rotate / 90) % 4;
        MediaPlayer.VideoRotate videoRotate = null;
        //默认0度
        switch (ro) {
            case 1:
                videoRotate = MediaPlayer.VideoRotate.ROTATE_90;
                break;
            case 2:
                videoRotate = MediaPlayer.VideoRotate.ROTATE_180;
                break;
            case 3:
                videoRotate = MediaPlayer.VideoRotate.ROTATE_270;
                break;
            default:
                videoRotate = MediaPlayer.VideoRotate.ROTATE_0;
        }
        getPlayer().setRenderRotate(videoRotate);
    }

    @Override
    public void bindControlPanel(HaruControlPanel panel) {
        this.controlPanel = panel;
        panel.onBindController(this);
        panel.onNormalSize();
    }

    @Override
    public int getBufferPosition() {
        return getPlayer().getBufferPosition();
    }

    @Override
    public int getCurVideoPosition() {
        return getPlayer().getCurrentPosition();
    }

    /**
     * 直播时无法明确视频长度 返回-1
     *
     * @return
     */
    @Override
    public int getVideoDuration() {
        return canControlProgress() ? getPlayer().getDuration() : -1;
    }

    /**
     * 设置播放位置
     */
    @Override
    public void setPlayPosition(int playPosition) {
        if (canControlProgress()) {
            //停止了就重新播放
            if (videoState == VideoState.STOP) {
                replay();
            }
            isSeeking = true;
            //如果不在缓存内
            curPlayPos = playPosition;
            getPlayer().seekTo(playPosition);
        }
    }

    /**
     * 阿里的实现的缓冲区 是以时长为准 类型是int
     * 貌似无效
     *
     * @param size 缓冲区的视频长度
     */
    @Override
    public void setBufferSize(long size) {
        maxBufferDuration = size;
        getPlayer().setMaxBufferDuration((int) size);
    }

    @Override
    public void destroy() {
        if (playingHandler != null) {
            playingHandler.removeCallbacksAndMessages(null);
            playingHandler = null;
        }
    }

    private void startMonitorPlaying() {
        if (playingHandler == null) {
            playingHandler = new Handler(this);
        }
        playingHandler.removeCallbacksAndMessages(null);
        playingHandler.sendEmptyMessageDelayed(1, bufferMonitorDelay);
    }

    private void stopMonitorPlaying() {
        if (playingHandler != null) {
            playingHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean canControlProgress() {
        return true;
    }

    @Override
    public void setOnCompletedListener(OnCompletedListener listener) {
        this.onCompletedListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    @Override
    public void setBrightness(float lPercent) {
        if (lPercent < 0) {
            lPercent = 0;
        } else if (lPercent > 100) {
            lPercent = 100;
        }
        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
//        if (brightness == -1) {
//            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
//        } else {
//            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
//        }
        lp.screenBrightness = lPercent / 100f;
        window.setAttributes(lp);
        Config config = Config.getInstance(getContext());
        config.brightness = lPercent;
        config.save(getContext());
    }

    @Override
    public float getBrightness() {
        return Config.getInstance(getContext()).brightness;
    }

    @Override
    public void setVolume(float vPercent) {
        getPlayer().setVolume((int) vPercent);
        Config config = Config.getInstance(getContext());
        config.volume = vPercent;
        config.save(getContext());
    }

    @Override
    public float getVolume() {
        return Config.getInstance(getContext()).volume;
    }

    private ViewGroup.LayoutParams normalLayout;

    private ViewGroup normalPar;

    @Override
    public void fullScreen(Activity activity) {
        if (!isFullScreen) {
            //获取面板
            normalLayout = controlPanel.getPanelView().getLayoutParams();
            //获取面板的父视图
            normalPar = (ViewGroup) controlPanel.getPanelView().getParent();
            //删除面板
            normalPar.removeView(controlPanel.getPanelView());
            //添加contentView 设置占满屏幕
            activity.getWindow().addContentView(controlPanel.getPanelView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            isFullScreen = true;

            controlPanel.onFullScreenSize();
        }
    }

    @Override
    public void normalSize() {
        if (isFullScreen) {

            ViewGroup viewGroup = (ViewGroup) controlPanel.getPanelView().getParent();
            //判断surfaceView是否已经在布局中
            viewGroup.removeView(controlPanel.getPanelView());

            normalPar.addView(controlPanel.getPanelView(), normalLayout);
            isFullScreen = false;
            controlPanel.onNormalSize();
        }
    }

    @Override
    public void toggleFullScreen(Activity activity) {
        if (isFullScreen) {
            normalSize();
        } else {
            fullScreen(activity);
        }
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    @Override
    public void checkPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 向用户解释为什么需要这个权限
            new AlertDialog.Builder(activity)
                    .setMessage("视频缓存需要文件读写权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //申请权限
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        }
                    })
                    .show();

        }
    }

    @Override
    public void setPlayRefer(String refer) {
        getPlayer().setRefer(refer);
    }

    @Override
    public int getVideoViewWidth() {
        return getWidth();
    }

    @Override
    public int getVideoViewHeight() {
        return getHeight();
    }

    /**
     * 判断播放器状态 如果是暂停 继续播放，如果是停止，获取暂停时保存的状态 然后重新加载
     */
    @Override
    public void resumePlay() {
        switch (videoState) {
            case PAUSE:
                //暂停状态才可以resume
                if (!getPlayer().isPlaying()) {
                    play();
                }
                break;
            case STOP:
                //重头开始播放
                replay();
                getPlayer().seekTo(curPlayPos);
                break;
            case PREPARING:
                replay();
                break;
        }
    }

    /***
     * 重新播放会调用onPrepared监听
     */
    @Override
    public void replay() {
        curPlayPos = 0;
        //重新设置一下播放地址
        setPlayUrl(this.playUrl);
    }

    @Override
    public void onPrepared() {
        videoState = VideoState.PREPARED;
        //准备完成后 开启 监听缓存
        startMonitorPlaying();
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared();
        }
    }

    /**
     * 监听缓存的回调
     */
    @Override
    public boolean handleMessage(Message message) {
        if (!isSeeking) {
            //不是预加载和加载状态再调用加载视频数据
            controlPanel.onPlayPositionChange(getCurVideoPosition(), getVideoDuration());
        }
        controlPanel.onBufferPositionChange(getBufferPosition(), getVideoDuration());
        playingHandler.sendEmptyMessageDelayed(1, bufferMonitorDelay);
        return true;
    }

    /**
     * 正在播放时调用
     */
    @Override
    public void onPcmData(byte[] bytes, int i) {
        if (videoState == VideoState.LOADING) {
            infoHolder = 0;
            videoState = VideoState.PLAY;
            controlPanel.onEndBufferLoading();
            //如果是预加载并且没有info的回调 表示不需要加载数据
        }
    }

    private int infoHolder = 0;

    /**
     * 从源码上看，这个回调是在加载的时候调用
     * 当i = 105时 pcmData 还会再调用一帧
     */
    @Override
    public void onInfo(int i, int i1) {
        //105是加载时的状态
        if ((videoState == VideoState.PLAY || videoState == VideoState.PREPARE_LOADING) && i == 105) {
            videoState = VideoState.PREPARE_LOADING;
            infoHolder++;
            if (infoHolder >= 2) {
                videoState = VideoState.LOADING;
                controlPanel.onStartBufferLoading();
            }
        }
    }

    /**
     * 完成之后是停止状态
     */
    @Override
    public void onCompleted() {
        stop();
        if (this.onCompletedListener != null) {
            this.onCompletedListener.onCompleted();
        }
    }

    @Override
    public void onError(int i, String s) {
        switch (videoState) {
            case PLAY:
                break;
            case LOADING:
                controlPanel.onEndBufferLoading();
                break;
        }
        if (this.onErrorListener != null) {
            this.onErrorListener.onError(i, s);
        }
    }

    @Override
    public void onFrameInfoListener() {
    }

    @Override
    public void onSeekCompleted() {
        isSeeking = false;
    }
}
