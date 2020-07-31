package com.wdl.tools.risk;

import android.content.Context;

import org.json.JSONObject;

/**
 * 检测设备环境
 * <p></p>
 * 是否ROOT
 * 是否安装多开软件
 * 是否处于调试状态
 * 是否是模拟器
 * 是否安装改机软件
 */
public final class LocalDeviceEnvUtil {

    /**
     * 获取设备环境信息
     *
     * @param context
     * @return
     */
    public static JSONObject getLocalDeviceEnv(Context context) {
        JSONObject extra = new JSONObject();
        try {
            extra.put("debugging", RiskUtils.checkIsDebuggerConnected() ? 1 : 0);         // 是否处于调试状态
            extra.put("multiple_app_on", RiskUtils.checkIsMultiByPkg(context) ? 1 : 0);   // 是否安装多开软件
            extra.put("root", RiskUtils.isRoot() ? 1 : 0);                                // 是否root
            extra.put("simulator", EmulatorCheckUtil.isEmulator(context) ? 1 : 0);        // 是否是模拟器
            extra.put("change_machine_software", RiskUtils.isExchangeSoft(context) ? 1 : 0);  // 是否安装该机软件                                  // 是否有改机操作

        } catch (Exception e) {
            e.printStackTrace();
        }
        return extra;
    }
}
