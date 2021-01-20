package com.wdl.tools.risk;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.text.TextUtils;
import android.util.Log;

import com.wdl.tools.command.CommandUtil;

import static android.content.Context.SENSOR_SERVICE;
import static com.wdl.tools.risk.EmulatorResult.RESULT_EMULATOR;
import static com.wdl.tools.risk.EmulatorResult.RESULT_MAYBE_EMULATOR;
import static com.wdl.tools.risk.EmulatorResult.RESULT_UNKNOWN;


/**
 * 检测是否是模拟器
 */
public final class EmulatorCheckUtil {
    private static final String TAG = "EmulatorCheckUtil";

    /**
     * 检测是否是模拟器
     * <p>蓝叠模拟器存在问题<p/>
     *
     * @param context
     * @return
     */
    public static boolean isEmulator(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context must not be null");

        int suspectCount = 0;

        //检测硬件名称
        EmulatorResult hardwareResult = checkFeaturesByHardware();
        switch (hardwareResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "hardware = " + hardwareResult.value);
                return true;
        }

        //检测渠道
        EmulatorResult flavorResult = checkFeaturesByFlavor();
        switch (flavorResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "flavor = " + flavorResult.value);
                return true;
        }

        //检测设备型号
        EmulatorResult modelResult = checkFeaturesByModel();
        switch (modelResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "model = " + modelResult.value);
                return true;
        }

        //检测硬件制造商
        EmulatorResult manufacturerResult = checkFeaturesByManufacturer();
        switch (manufacturerResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "manufacturer = " + manufacturerResult.value);
                return true;
        }

        //检测主板名称
        EmulatorResult boardResult = checkFeaturesByBoard();
        switch (boardResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "board = " + boardResult.value);
                return true;
        }

        //检测主板平台
        EmulatorResult platformResult = checkFeaturesByPlatform();
        switch (platformResult.result) {
            case RESULT_MAYBE_EMULATOR:
                ++suspectCount;
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "platform = " + platformResult.value);
                return true;
        }

        //检测基带信息
        EmulatorResult baseBandResult = checkFeaturesByBaseBand();
        switch (baseBandResult.result) {
            case RESULT_MAYBE_EMULATOR:
                suspectCount += 2;//模拟器基带信息为null的情况概率相当大
                break;
            case RESULT_EMULATOR:
                Log.i(TAG, "baseBand = " + baseBandResult.value);
                return true;
        }

        //检测传感器数量
        int sensorNumber = getSensorNumber(context);
        if (sensorNumber <= 7) ++suspectCount;

        //检测是否支持闪光灯
        boolean supportCameraFlash = supportCameraFlash(context);
        if (!supportCameraFlash) ++suspectCount;
        //检测是否支持相机
        boolean supportCamera = supportCamera(context);
        if (!supportCamera) ++suspectCount;
        //检测是否支持蓝牙
        boolean supportBluetooth = supportBluetooth(context);
        if (!supportBluetooth) ++suspectCount;

        //检测光线传感器
        boolean hasLightSensor = hasLightSensor(context);
        if (!hasLightSensor) ++suspectCount;

        //检测进程组信息
        EmulatorResult cgroupResult = checkFeaturesByCgroup();
        if (cgroupResult.result == RESULT_MAYBE_EMULATOR) ++suspectCount;

        StringBuilder sb = new StringBuilder("Test start")
                .append("\r\n").append("hardware = ").append(hardwareResult.value)
                .append("\r\n").append("flavor = ").append(flavorResult.value)
                .append("\r\n").append("model = ").append(modelResult.value)
                .append("\r\n").append("manufacturer = ").append(manufacturerResult.value)
                .append("\r\n").append("board = ").append(boardResult.value)
                .append("\r\n").append("platform = ").append(platformResult.value)
                .append("\r\n").append("baseBand = ").append(baseBandResult.value)
                .append("\r\n").append("sensorNumber = ").append(sensorNumber)
                .append("\r\n").append("supportCamera = ").append(supportCamera)
                .append("\r\n").append("supportCameraFlash = ").append(supportCameraFlash)
                .append("\r\n").append("supportBluetooth = ").append(supportBluetooth)
                .append("\r\n").append("hasLightSensor = ").append(hasLightSensor)
                .append("\r\n").append("cgroupResult = ").append(cgroupResult.value)
                .append("\r\n").append("suspectCount = ").append(suspectCount);
        Log.i(TAG, sb.toString());

        //嫌疑值大于2，认为是模拟器，非准确值
        return suspectCount > 2;
    }

    private static int getUserAppNum(String userApps) {
        if (TextUtils.isEmpty(userApps)) return 0;
        String[] result = userApps.split("package:");
        return result.length;
    }

    private static String getProperty(String propName) {
        String property = CommandUtil.getProperty(propName);
        return TextUtils.isEmpty(property) ? null : property;
    }

    /**
     * 特征参数-硬件名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByHardware() {
        String hardware = getProperty("ro.hardware");
        if (null == hardware)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = hardware.toLowerCase();
        switch (tempValue) {
            case "ttvm"://天天模拟器
            case "nox"://夜神模拟器
            case "cancro"://网易MUMU模拟器
            case "intel"://逍遥模拟器
            case "vbox":
            case "vbox86"://腾讯手游助手
            case "android_x86"://雷电模拟器
                result = RESULT_EMULATOR;
                break;
            default:
                result = RESULT_UNKNOWN;
                break;
        }
        return new EmulatorResult(result, hardware);
    }

    /**
     * 特征参数-渠道
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByFlavor() {
        String flavor = getProperty("ro.build.flavor");
        if (null == flavor)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = flavor.toLowerCase();
        if (tempValue.contains("vbox")) result = RESULT_EMULATOR;
        else if (tempValue.contains("sdk_gphone")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, flavor);
    }

    /**
     * 特征参数-设备型号
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByModel() {
        String model = getProperty("ro.product.model");
        if (null == model)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = model.toLowerCase();
        if (tempValue.contains("google_sdk")) result = RESULT_EMULATOR;
        else if (tempValue.contains("emulator")) result = RESULT_EMULATOR;
        else if (tempValue.contains("android sdk built for x86"))
            result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, model);
    }

    /**
     * 特征参数-硬件制造商
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByManufacturer() {
        String manufacturer = getProperty("ro.product.manufacturer");
        if (null == manufacturer)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = manufacturer.toLowerCase();
        if (tempValue.contains("genymotion")) result = RESULT_EMULATOR;
        else if (tempValue.contains("netease"))
            result = RESULT_EMULATOR;//网易MUMU模拟器
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, manufacturer);
    }

    /**
     * 特征参数-主板名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByBoard() {
        String board = getProperty("ro.product.board");
        if (null == board)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = board.toLowerCase();
        if (tempValue.contains("android")) result = RESULT_EMULATOR;
        else if (tempValue.contains("goldfish")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, board);
    }

    /**
     * 特征参数-主板平台
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByPlatform() {
        String platform = getProperty("ro.board.platform");
        if (null == platform)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        String tempValue = platform.toLowerCase();
        if (tempValue.contains("android")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, platform);
    }

    /**
     * 特征参数-基带信息
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private static EmulatorResult checkFeaturesByBaseBand() {
        String baseBandVersion = getProperty("gsm.version.baseband");
        if (null == baseBandVersion)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        int result;
        if (baseBandVersion.contains("1.0.0.0")) result = RESULT_EMULATOR;
        else result = RESULT_UNKNOWN;
        return new EmulatorResult(result, baseBandVersion);
    }

    /**
     * 获取传感器数量
     */
    private static int getSensorNumber(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return sm.getSensorList(Sensor.TYPE_ALL).size();
    }

    /**
     * 获取已安装第三方应用数量
     */
    private static int getUserAppNumber() {
        String userApps = CommandUtil.exec("pm list package -3");
        return getUserAppNum(userApps);
    }

    /**
     * 是否支持相机
     */
    private static boolean supportCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 是否支持闪光灯
     */
    private static boolean supportCameraFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * 是否支持蓝牙
     */
    private static boolean supportBluetooth(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    /**
     * 判断是否存在光传感器来判断是否为模拟器
     * 部分真机也不存在温度和压力传感器。其余传感器模拟器也存在。
     *
     * @return false为模拟器
     */
    private static boolean hasLightSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光线传感器
        if (null == sensor) return false;
        else return true;
    }

    /**
     * 特征参数-进程组信息
     */
    private static EmulatorResult checkFeaturesByCgroup() {
        String filter = CommandUtil.exec("cat /proc/self/cgroup");
        if (null == filter)
            return new EmulatorResult(RESULT_MAYBE_EMULATOR, null);
        return new EmulatorResult(RESULT_UNKNOWN, filter);
    }

    /**
     * 通过电池以及温度检测
     *
     * @param context Context
     * @return
     */
    private static EmulatorResult isAdopt(Context context) {
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = context.registerReceiver(null, intentFilter);
        int voltage = batteryStatusIntent.getIntExtra("voltage", 99999);
        int temperature = batteryStatusIntent.getIntExtra("temperature", 99999);
        if (((voltage == 0) && (temperature == 0))
                || ((voltage == 10000) && (temperature == 0))) {
            //这是通过电池的伏数和温度来判断是真机还是模拟器
            return new EmulatorResult(RESULT_EMULATOR, null);
        } else {
            return new EmulatorResult(RESULT_UNKNOWN, null);
        }
    }
}
