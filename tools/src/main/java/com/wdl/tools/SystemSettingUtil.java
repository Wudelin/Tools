package com.wdl.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author: wdl
 * @date: 2021/1/20
 * <p>
 * 1.设置屏幕亮度-APP级别
 * 2.设置屏幕亮度-系统级别
 * 3.设置媒体音量
 */
public class SystemSettingUtil {

    /**
     * 设置屏幕亮度-APP界别
     * <p>
     * TODO 系统级别 需系统权限
     *
     * @param activity   Activity
     * @param brightness brightness
     */
    public static void setScreenBrightness(Activity activity, int brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    /**
     * 获取屏幕亮度
     *
     * @param context Context
     * @return int
     */
    public static int getScreenBrightness(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }


    /**
     * 设置系统媒体音量
     *
     * @param context Context
     * @param volume  volume
     */
    public static void setVolume(Context context, int volume) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //设置音量大小，
        // 第一个参数：STREAM_VOICE_CALL（通话）、STREAM_SYSTEM（系统声音）、STREAM_RING（铃声）、STREAM_MUSIC（音乐）和STREAM_ALARM（闹铃）；
        // 第二个参数：音量值，取值范围为0-7；
        // 第三个参数：可选标志位，用于显示出音量调节UI（AudioManager.FLAG_SHOW_UI）。
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }


    /**
     * 获取系统媒体音量
     *
     * @param context Context
     * @return volume
     */
    public static int getVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}
