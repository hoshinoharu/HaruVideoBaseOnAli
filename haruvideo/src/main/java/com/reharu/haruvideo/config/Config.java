package com.reharu.haruvideo.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hoshino on 2018/6/26.
 */

public class Config {
    private float brightness = 50f;
    private float volume = 0f;
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

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        if (brightness < 0) {
            this.brightness = 0;
        } else if (brightness > 100) {
            this.brightness = 100;
        } else {
            this.brightness = brightness;
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0) {
            this.volume = 0;
        } else if (volume > 100) {
            this.volume = 100;
        } else {
            this.volume = volume;
        }

    }
}
