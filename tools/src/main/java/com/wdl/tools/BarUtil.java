package com.wdl.tools;

import android.app.Activity;
import android.view.View;

/**
 * 状态栏相关工具类
 */
public final class BarUtil {
    private BarUtil() {

    }

    /**
     * 隐藏顶部、底部导航栏
     *
     * @param mCurrentActivity Activity
     */
    public static void hideBar(final Activity mCurrentActivity) {
        mCurrentActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mCurrentActivity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //布局位于状态栏下方
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //隐藏导航栏
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                mCurrentActivity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }
}
