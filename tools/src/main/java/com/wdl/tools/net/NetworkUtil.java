package com.wdl.tools.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 网络工具类
 * <p>
 * 1. 获取网络运营商类型
 * 2. 获取蜂窝数据类型（WiFi、数据）
 * 3. 手机网络类型
 */
public final class NetworkUtil {
    /**
     * 运营商网络类型
     */
    public static final String OPERATOR_TYPE_CMCC = "cmcc";        //移动
    public static final String OPERATOR_TYPE_CTCC = "ctcc";        //电信
    public static final String OPERATOR_TYPE_CUCC = "cucc";        //联通
    public static final String OPERATOR_TYPE_UNKNOWN = "unknown";        //未知运营商

    /**
     * 数据类型
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;            //未知
    public static final int NETWORK_TYPE_DATA = 1;                //流量
    public static final int NETWORK_TYPE_WIFI = 2;                //wifi；
    public static final int NETWORK_TYPE_DATA_AND_WIFI = 3;     //数据流量+wifi

    /**
     * 各运营商Operator码
     */
    private static final List<String> mCuccOperator = Arrays.asList("46001", "46006", "46009");
    private static final List<String> mCmccOperator = Arrays.asList("46000", "46002", "46004", "46007");
    private static final List<String> mCtccOperator = Arrays.asList("46003", "46005", "46011");


    private NetworkUtil() {
        throw new UnsupportedOperationException("u can not operation me...");
    }


    /**
     * 获取网络运营商 cmcc.移动流量 cucc.联通流量网络 ctcc.电信流量网络 unknown.未知
     *
     * @return operatorType
     */
    public static String networkOperatorType(Context context) {
        return getCellularOperatorType(context);
    }

    /**
     * networkType 网络状态：0未知；1流量 2 wifi；3 数据流量+wifi
     */
    public static int networkType(Context context) {
        return checkNetworkConnection(context);
    }

    /**
     * 获取当前网络环境信息
     *
     * @param context 上下文
     * @return 返回JSON 格式的网络信息
     */
    public static String getNetworkInfo(Context context) {
        JSONObject info = new JSONObject();
        try {
            info.put("operatorType", getCellularOperatorType(context));
            info.put("networkType", "" + checkNetworkConnection(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info.toString();
    }


    /**
     * 判断网络连接状态/数据类型
     *
     * @param context Context
     * @return 0:无连接 1:数据 2:wifi 3:数据+wifi
     */
    private static int checkNetworkConnection(Context context) {
        final ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == mConnectivityManager) {
            return NETWORK_TYPE_UNKNOWN;
        }

        final android.net.NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // 数据流量是否打开
        final boolean isData = isDataEnable(context);
        if (wifi == null && !isData) {
            return NETWORK_TYPE_UNKNOWN;
        }

        // 数据 + wifi
        if (wifi.isAvailable() && isData) {
            return NETWORK_TYPE_DATA_AND_WIFI;
        } else if (wifi.isAvailable() && !isData) {
            // wifi
            return NETWORK_TYPE_WIFI;
        } else if (!wifi.isAvailable() && isData) {
            return NETWORK_TYPE_DATA;
        } else {
            return NETWORK_TYPE_UNKNOWN;
        }
    }

    /**
     * 判断数据流量是否打开
     *
     * @param context Context
     * @return 数据流量是否打开
     */
    private static boolean isDataEnable(Context context) {
        try {
            final Method method = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (boolean) method.invoke(cm);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取数据流量sim卡的运营商信息
     *
     * @return 1：移动 2联通 3电信 0未知
     */
    private static String getCellularOperatorType(final Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return OPERATOR_TYPE_UNKNOWN;
        }
        final String operator = tm.getSimOperator();
        if (TextUtils.isEmpty(operator)) {
            return OPERATOR_TYPE_UNKNOWN;
        }
        // 中国联通
        if (mCuccOperator.contains(operator)) {
            return OPERATOR_TYPE_CUCC;
            // 中国移动
        } else if (mCmccOperator.contains(operator)) {
            return OPERATOR_TYPE_CMCC;
            // 中国电信
        } else if (mCtccOperator.contains(operator)) {
            return OPERATOR_TYPE_CTCC;
        } else {
            return OPERATOR_TYPE_UNKNOWN;
        }
    }


    /**
     * 转换为ip格式
     */
    public static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }


    /**
     * 查看sim 状态是否可用
     *
     * @param context 上下文
     * @return 是否可用
     */
    public static boolean isSimStateAvailable(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return TelephonyManager.SIM_STATE_READY == tm.getSimState();
        }
        return false;
    }

    /**
     * 查看sim 数据网络是否可用
     *
     * @param context 上下文
     * @return 是否可用
     */
    @SuppressLint("DiscouragedPrivateApi")
    public static boolean isSimDataAvailable(Context context) {
        try {
            if (isSimStateAvailable(context)) {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                Method var3 = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
                var3.setAccessible(true);
                Object isConnect = var3.invoke(connMgr);
                return isConnect == null ? false : (Boolean) isConnect;
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
        return false;
    }

    /**
     * 查看网络是否可用
     *
     * @param context s
     * @return 是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }


    /**
     * 判断是否存在sim卡
     *
     * @param context Context
     * @return
     */
    private static boolean hasSim(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

}
