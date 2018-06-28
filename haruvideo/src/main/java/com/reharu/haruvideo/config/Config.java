package com.reharu.haruvideo.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hoshino on 2018/6/26.
 */

public class Config {
    public float brightness = 50f;
    public float volume = 0f;
    private static Config config;

    private Config() {

    }

    /**
     * 获取配置
     *
     * @param context
     */
    public static Config getInstance(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.class.getName(), Context.MODE_PRIVATE);
        if (config == null) {
            config = new Config();
            config.brightness = sharedPreferences.getFloat("brightness", config.brightness);
            config.volume = sharedPreferences.getFloat("volume", config.volume);
        }
        return config;
    }

    /**
     * 保存配置
     *
     * @param context
     */
    public void save(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.class.getName(), Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putFloat("brightness", brightness)
                .putFloat("volume", volume)
                .apply();
    }

}
