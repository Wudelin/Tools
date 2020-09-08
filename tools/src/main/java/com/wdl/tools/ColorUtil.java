package com.wdl.tools;

import android.graphics.Color;

/**
 * Create by: wdl at 2019/11/8 15:44
 * 颜色转换相关
 * 00000000 00000000 00000000 00000000
 * A      R       G        B
 */
public final class ColorUtil {
    private ColorUtil() {
    }

    /**
     * int 转 argb
     *
     * @param color color
     * @return ARGB
     */
    public static int intToARGB(final int color) {
        final int colorA = (color >> 24) & 0xff;
        final int colorR = (color >> 16) & 0xff;
        final int colorG = (color >> 8) & 0xff;
        final int colorB = color & 0xff;

        return colorA | colorR | colorG | colorB;
    }

    /**
     * 计算渐变颜色中间色值
     *
     * @param startColor 起始颜色
     * @param endColor   结束颜色
     * @param ratio      百分比，取值范围【0~1】
     * @return 颜色值
     */
    public static int getColor(int startColor, int endColor, float ratio) {
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);


        int red = (int) (redStart + ((redEnd - redStart) * ratio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * ratio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * ratio + 0.5));
        return Color.argb(255, red, greed, blue);
    }
}
