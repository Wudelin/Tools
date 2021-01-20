package com.wdl.tools;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * <p>
 * 1.重启APP
 * 2.双击返回键退出
 * 3.退出程序
 * 4.获取进程名
 */
@SuppressWarnings("unused")
public final class WProcessUtil {

    /**
     * 缓存用
     */
    private static String sProcessName = "";
    private static long exitTime = 0L;

    public interface ExitPromoteListener {
        void toast();
    }

    private WProcessUtil() {

    }

    /**
     * 获取进程名
     * <p>
     * 解决部分机型获取抛异常的问题
     *
     * @param context Context
     * @return String
     */
    public static String getCurrentProcessName(Context context) {
        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        try {
            sProcessName = getProcessNameByAM(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        try {
            sProcessName = getProcessNameByApplication();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        try {
            sProcessName = getProcessNameByCmd();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        try {
            sProcessName = getProcessNameByActivityThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sProcessName;
    }

    /**
     * 反射获取
     *
     * @return String
     */
    private static String getProcessNameByActivityThread() {
        String processName = null;
        try {
            @SuppressLint("PrivateApi") final Method declaredMethod = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
                    .getDeclaredMethod("currentProcessName", new Class[0]);
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null, new Object[0]);
            if (invoke instanceof String) {
                processName = (String) invoke;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processName;
    }

    /**
     * 命令行获取
     *
     * @return String
     */
    private static String getProcessNameByCmd() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 通过Application获取进程名
     *
     * @return String
     */
    public static String getProcessNameByApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        return null;
    }


    /**
     * 获取当前进程名
     *
     * @param context Context
     * @return 当前APP进程名
     */
    public static String getProcessNameByAM(Context context) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        final List<ActivityManager.RunningAppProcessInfo> process = am.getRunningAppProcesses();
        if (process == null || process.isEmpty()) return null;
        final int pId = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo item : process) {
            if (item.pid == pId) return item.processName;
        }
        return null;
    }


    /**
     * 结束
     */
    public static void exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    /**
     * 按返回键退出
     *
     * @param event           KeyEvent
     * @param intervalTime    间隔时间
     * @param promoteListener ExitPromoteListener
     * @return 是否处理过事件
     */
    public static boolean dce(KeyEvent event, long intervalTime, ExitPromoteListener promoteListener) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > intervalTime) {
                exitTime = System.currentTimeMillis();
                // 提示
                if (promoteListener != null) {
                    promoteListener.toast();
                }
            } else {
                exitApp();
            }
            return true;
        }
        return false;
    }

//    /**
//     * 重启APP
//     *
//     * @param context Context
//     */
//    @Deprecated
//    public static void restartApp(Context context) {
//        Intent reboot = context.getPackageManager()
//                .getLaunchIntentForPackage(context.getPackageName());
//        PendingIntent restartIntent = PendingIntent.getActivity(context, 0, reboot, 0);
//        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//        exitApp();
//    }


    /**
     * 重启APP
     *
     * @param context Context
     */
    public static void restartApp(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return;
        }
        final Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
        exitApp();
    }
}
