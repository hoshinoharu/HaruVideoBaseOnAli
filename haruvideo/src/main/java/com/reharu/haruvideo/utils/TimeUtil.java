package com.reharu.haruvideo.utils;

import java.util.Locale;

/**
 * Created by hoshino on 2018/6/27.
 */

public class TimeUtil {
    //毫秒转
    public static String formatLongTimeTommss(long mills) {
        long allSeconds = mills / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds % 60;
        return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
    }
}
