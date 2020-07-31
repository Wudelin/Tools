package com.wdl.tools.prompt;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Toast工具类
 * <p>
 * 可在子线程调用
 */
@SuppressWarnings("unused")
public final class ToastUtil {
    private ToastUtil() {
        throw new UnsupportedOperationException("u can not operation me...");
    }

    /**
     * show
     *
     * @param context      上下文
     * @param charSequence CharSequence
     */
    public static void show(final Context context, final CharSequence charSequence) {
        show(context, charSequence, Toast.LENGTH_SHORT, 0);
    }

    /**
     * @param charSequence CharSequence
     * @param gravity      显示的位置
     */
    public static void show(final Context context, final CharSequence charSequence, final int gravity) {
        show(context, charSequence, Toast.LENGTH_SHORT, gravity);
    }

    /**
     * @param context      上下文
     * @param charSequence CharSequence
     * @param duration     持续时间
     * @param gravity      显示的位置
     */
    public static void show(final Context context, final CharSequence charSequence, final int duration, final int gravity) {
        // 判断是否在主线程的looper
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showInternal(context, charSequence, duration, gravity);
        } else {
            // 发送一条显示toast的消息,传入getMainLooper，可直接在子线程中调用
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showInternal(context, charSequence, duration, gravity);
                }
            });
        }
    }

    /**
     * 真实的显示toast
     *
     * @param charSequence CharSequence
     * @param duration     持续时间
     * @param gravity      显示的位置
     */
    private static void showInternal(final Context context, CharSequence charSequence, int duration, final int gravity) {
        if (TextUtils.isEmpty(charSequence)) return;
        Toast toast = Toast.makeText(context, charSequence, duration);
        if (gravity != 0) {
            toast.setGravity(gravity, 0, 0);
        }
        if (toast != null)
            toast.show();
    }
}
