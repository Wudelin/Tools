package com.wdl.tools.screen;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author: wdl
 * @date: 2021/3/5
 * <p>
 * 今日头条屏幕适配方案
 * <p>
 * px = dp * density    => dp固定，要使得px自适应，只需改变density值即可
 * density = dpi / 160
 * px = dp * dpi / 160
 */
public class AutoSizeUtil {
    /**
     * 系统Density
     */
    private static float mDensity;
    /**
     * 系统字体相关
     */
    private static float mScaleDensity;

    /**
     * dp固定，要使得px自适应，只需改变density值即可
     *
     * @param activity    Activity
     * @param application Application
     * @param dp          设计稿宽度dp
     */
    public static void autoScreenSize(@Nullable final Activity activity, @Nullable final Application application, float dp) {
        if (application == null || activity == null) {
            throw new NullPointerException("application or activity is null");
        }

        final DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (displayMetrics == null) {
            throw new NullPointerException("displayMetrics is null");
        }

        if (mDensity == 0) {
            mDensity = displayMetrics.density;
            mScaleDensity = displayMetrics.scaledDensity;

            // 解决系统切换字体大小时，app内字体大小不改变的问题
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(@NonNull Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        mScaleDensity = displayMetrics.scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }

        // px = dp * density => density = px / dp   dp 为设计稿的宽
        // 实际的密度
        final float mTargetDensity = displayMetrics.widthPixels / dp;
        // density / mTargetDensity = scaleDensity / mTargetScaleDensity => mTargetScaleDensity = scaleDensity / (density / mTargetDensity)
        // 字体缩放值
        final float mTargetScaleDensity = mTargetDensity * mScaleDensity / mDensity;
        // 实际像素密度
        // density = dpi / 160 => dpi = density * 160
        final int mTargetDensityDpi = (int) (mTargetDensity * 160);

        displayMetrics.density = mTargetDensity;
        displayMetrics.scaledDensity = mTargetScaleDensity;
        displayMetrics.densityDpi = mTargetDensityDpi;


        final DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = mTargetDensity;
        activityDisplayMetrics.scaledDensity = mTargetScaleDensity;
        activityDisplayMetrics.densityDpi = mTargetDensityDpi;
    }
}
