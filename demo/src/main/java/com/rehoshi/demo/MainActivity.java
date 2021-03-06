package com.rehoshi.demo;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.reharu.haruvideo.config.Config;
import com.reharu.haruvideo.control.HaruVideoController;
import com.reharu.haruvideo.controlpanel.HaruControlPanel;
import com.rehoshi.demo.widget.HaruControlPanelImpl;

public class MainActivity extends AppCompatActivity implements HaruControlPanel.PanelStateListener {

    HaruVideoController videoController;
    HaruControlPanelImpl controlPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.hr_v_demo_activity);
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }

        String playUrl = "http://player.alicdn.com/video/aliyunmedia.mp4";
//        String playUrl = "http://livepull.iflysse.com/IflysseLive/test_ld.flv?auth_key=1530087842-0-0-4708a3f617e27867683d75d4efdc944d" ;
        controlPanel = this.findViewById(R.id.panel);

        videoController = this.findViewById(R.id.player);

        videoController.bindControlPanel(controlPanel);

        videoController.setOnVideoPreparedListener(() -> {
            videoController.play();
        });

        videoController.setPlayUrl(playUrl);

        videoController.setBrightness(Config.getInstance(this).getBrightness());

        videoController.setVolume(Config.getInstance(this).getVolume());

        videoController.checkPermission(this);

        controlPanel.addPanelStateListener(this);

        controlPanel.getBackBtn().setOnClickListener(v -> {
            if (videoController.isFullScreen()) {
                videoController.normalSize();
            } else {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoController.pause();
    }


    @Override
    protected void onStop() {
        super.onStop();
//        videoController.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoController.resumePlay();
    }

    @Override
    public void onFullScreenSize() {
        //横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onNormalSize() {
        //竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
