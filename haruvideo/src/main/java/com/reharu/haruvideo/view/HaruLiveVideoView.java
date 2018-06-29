package com.reharu.haruvideo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by hoshino on 2018/6/29.
 * 播放直播的View
 */
public class HaruLiveVideoView extends HaruVideoView {
    public HaruLiveVideoView(@NonNull Context context) {
        super(context);
    }

    public HaruLiveVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HaruLiveVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canControlProgress() {
        return false;
    }
}
