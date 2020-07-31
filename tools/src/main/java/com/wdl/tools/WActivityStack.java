package com.wdl.tools;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unused")
public final class WActivityStack {
    private static WActivityStack instance;
    // activity 管理栈
    private final List<Activity> mActivityStack = new CopyOnWriteArrayList<>();
    // 是否为调试状态
    private boolean isDebug = false;
    // 利用application.registerActivityLifecycleCallbacks管理Activity
    private Application application;

    private WActivityStack() {
    }

    /**
     * 获取单例对象
     *
     * @return WActivityStack
     */
    public static WActivityStack getInstance() {
        if (instance == null) {
            synchronized (WActivityStack.class) {
                if (instance == null) {
                    instance = new WActivityStack();
                }
            }
        }
        return instance;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    /**
     * 初始化注册ActivityLifecycleCallbacks
     *
     * @param application Application
     */
    public synchronized void init(Application application) {
        if (this.application == null) {
            this.application = application;
            this.application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        }
    }

    /**
     * Activity生命周期回调
     */
    private final Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
            addActivity(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            final int index = mActivityStack.indexOf(activity);
            if (index < 0)
                return;
            final int size = getSize();
            if (size <= 1)
                return;
            // 位于栈顶,先移除后添加
            if (index != (size - 1)) {
                if (isDebug)
                    WLogger.e(WActivityStack.class.getSimpleName() + " start " + activity + " old index " + index);
                removeActivity(activity);
                addActivity(activity);
                if (isDebug)
                    WLogger.e(WActivityStack.class.getSimpleName() + " end " + activity + " new index " + mActivityStack.indexOf(activity));
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            removeActivity(activity);
        }
    };

    /**
     * 添加Activity
     *
     * @param activity Activity
     */
    private void addActivity(Activity activity) {
        if (containsActivity(activity))
            return;
        mActivityStack.add(activity);
        if (isDebug)
            WLogger.e(WActivityStack.class.getSimpleName() + "+++++ " + activity + " " + mActivityStack.size()
                    + "\r\n" + getCurrentStack());
    }

    /**
     * 移除Activity
     *
     * @param activity Activity
     */
    private void removeActivity(Activity activity) {
        if (mActivityStack.remove(activity)) {
            if (isDebug)
                WLogger.e(WActivityStack.class.getSimpleName() + "+++++ " + activity + " " + mActivityStack.size()
                        + "\r\n" + getCurrentStack());
        }
    }

    /**
     * 获取全部Activity栈的字符串
     *
     * @return String
     */
    private String getCurrentStack() {
        return Arrays.toString(mActivityStack.toArray());
    }

    /**
     * 获取数量
     *
     * @return 栈中Activity的个数
     */
    public int getSize() {
        return mActivityStack.size();
    }

    /**
     * 获取指定下标的Activity
     *
     * @param index 下标
     * @return Activity
     */
    public Activity getActivity(int index) {
        try {
            return mActivityStack.get(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取最后一个Activity
     *
     * @return Activity
     */
    public Activity getLastActivity() {
        return getActivity(getSize() - 1);
    }

    /**
     * 是否包含某个对象
     *
     * @param activity Activity
     * @return true
     */
    public boolean containsActivity(Activity activity) {
        return mActivityStack.contains(activity);
    }

    /**
     * 结束所有对象
     */
    public void finishAllActivity() {
        for (Activity activity : mActivityStack) {
            activity.finish();
        }
    }

    /**
     * 返回栈中所有指定类型的对象
     *
     * @param clazz Class
     * @return List<Activity>
     */
    public List<Activity> getActivities(Class<? extends Activity> clazz) {
        final List<Activity> mList = new ArrayList<>(1);
        for (Activity activity : mActivityStack) {
            if (activity.getClass() == clazz)
                mList.add(activity);
        }
        return mList;
    }

    /**
     * 返回栈中所有指定类型的第一个对象
     *
     * @param clazz Class
     * @return Activity
     */
    public Activity getFirstActivity(Class<? extends Activity> clazz) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass() == clazz)
                return activity;
        }
        return null;
    }

    /**
     * 栈中是否包含指定类型的对象
     *
     * @param clazz Class
     * @return true 空-不包含
     */
    public boolean containsClazzActivity(Class<? extends Activity> clazz) {
        return getFirstActivity(clazz) == null;
    }

    /**
     * 结束指定类型的所有对象
     *
     * @param clazz Class
     */
    public void finishClassActivities(Class<? extends Activity> clazz) {
        final List<Activity> mList = getActivities(clazz);
        for (Activity activity : mList) {
            activity.finish();
        }
    }

    /**
     * 结束除了clazz之外的所有对象
     *
     * @param clazz Class
     */
    public void finishExcClassActivities(Class<? extends Activity> clazz) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass() != clazz)
                activity.finish();
        }
    }

    /**
     * 结束到某个类型的Activity(最后一个)
     *
     * @param clazz Class
     */
    public void finish2ClassActivity(Class<? extends Activity> clazz) {
        // 不包含clazz类型的activity直接结束
        if (containsClazzActivity(clazz))
            return;
        final int size = getSize();
        for (int i = size - 1; i >= 0; i--) {
            Activity activity = mActivityStack.get(i);
            if (activity.getClass() != clazz)
                activity.finish();
        }
    }


    /**
     * 1、 获取ActivityThread中保存的所有的ActivityRecord
     * 2 、从ActivityRecord中获取状态不是pause的Activity并返回，这个Activity就是当前处于活动状态的Activity
     *
     * @return
     */
    public static Activity getTopActivityInstance() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            if (activities == null || activities.isEmpty()) {
                return null;
            }
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

}
