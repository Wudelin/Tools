package com.wdl.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * 获取imsi
 */
public class ImsiUtil {
    public static final int INVALID_SUBSCRIPTION_ID = -1;

    public static String getPhoneIMSI(Context context, TelephonyManager tm) {
        String strImsi = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                strImsi = "";
            } else {
                if (context.getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    strImsi = "";
                } else {
                    int defaultDataSubId = ImsiUtil.getPreDataSubId(context);
                    strImsi = ImsiUtil.getPhoneIMSI(tm, defaultDataSubId);
                    return TextUtils.isEmpty(strImsi) ? "" : strImsi;
                }
            }
        } catch (Throwable error) {
            strImsi = "";
        }
        return strImsi;
    }

    /**
     * @param tm    TelephonyManager
     * @param subId 那张卡的subId
     * @return 返回 IMSI 信息
     */
    public static String getPhoneIMSI(TelephonyManager tm, int subId) {
        String strImsi = null;
        if (tm == null) {
            strImsi = null;
        }
        if (subId > -1000) {
            try {
                Method method;
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    method = tm.getClass().getMethod("getSubscriberId", long.class);
                    method.setAccessible(true);
                    strImsi = String.valueOf(method.invoke(tm, (long) subId));
                } else {
                    method = tm.getClass().getMethod("getSubscriberId", int.class);
                    method.setAccessible(true);
                    strImsi = String.valueOf(method.invoke(tm, subId));
                }
            } catch (Exception e) {
                e.printStackTrace();
                strImsi = getAssistantImsi(subId);
            }
        }
        return strImsi;
    }

    /**
     * 判断当前那张卡 是走流量的
     */
    public static Integer getPreDataSubId(Context context) {
        Integer subId = getDefaultDataSubId(context);
        if (!isValidSubscriptionId(subId)) {
            subId = getDefaultDataSubscriptionId(context);
            if (!isValidSubscriptionId(subId)) {
                subId = getDefaultSubscriptionId(context);
            }
        }
        return subId;
    }


    private static Integer getDefaultDataSubId(Context context) {
        Integer subId = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subManager = SubscriptionManager.from(context.getApplicationContext());
                Method getSubIdMethod = subManager.getClass().getMethod("getDefaultDataSubId");
                getSubIdMethod.setAccessible(true);
                subId = (int) getSubIdMethod.invoke(subManager);
            }
        } catch (Exception e) {
        }
        return subId;
    }

    private static Integer getDefaultDataSubscriptionId(Context context) {
        Integer subId = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SubscriptionManager subManager = context.getSystemService(SubscriptionManager.class);
                Method getSubIdMethod = subManager.getClass().getMethod("getDefaultDataSubscriptionId");
                getSubIdMethod.setAccessible(true);
                subId = (int) getSubIdMethod.invoke(subManager);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subManager = SubscriptionManager.from(context.getApplicationContext());
                Method getSubIdMethod = subManager.getClass().getMethod("getDefaultDataSubscriptionId");
                getSubIdMethod.setAccessible(true);
                subId = (int) getSubIdMethod.invoke(subManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subId;
    }

    private static Integer getDefaultSubscriptionId(Context context) {
        Integer subId = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SubscriptionManager subManager = context.getSystemService(SubscriptionManager.class);
                Method getSubIdMethod = subManager.getClass().getMethod("getDefaultSubscriptionId");
                getSubIdMethod.setAccessible(true);
                subId = (int) getSubIdMethod.invoke(subManager);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subManager = SubscriptionManager.from(context.getApplicationContext());
                Method getSubIdMethod = subManager.getClass().getMethod("getDefaultSubscriptionId");
                getSubIdMethod.setAccessible(true);
                subId = (int) getSubIdMethod.invoke(subManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subId;
    }

    /**
     * 高通平台有单独的类 android.telephony.MSimTelephonyManager
     */
    private static String getAssistantImsi(int subId) {
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName("android.telephony.MSimTelephonyManager");

            Method getdefault = telephonyClass.getMethod("getPhoneIMSI");
            Object telephonyManager = getdefault.invoke(null);
            Method method;
            try {
                method = telephonyClass.getMethod("getPhoneIMSI", int.class);
                method.setAccessible(true);
                return String.valueOf(method.invoke(telephonyManager, subId));
            } catch (Exception e) {
                try {
                    method = telephonyClass.getMethod("getPhoneIMSI", long.class);
                    method.setAccessible(true);
                    return String.valueOf(method.invoke(telephonyManager, (long) subId));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return true if a valid subId else false
     */
    private static boolean isValidSubscriptionId(int subId) {
        return subId > INVALID_SUBSCRIPTION_ID;
    }

}
