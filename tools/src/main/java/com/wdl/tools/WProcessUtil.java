package com.wdl.tools;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unused")
public final class WProcessUtil {

    /**
     * 缓存用
     */
    private static String sProcessName = "";

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
}
