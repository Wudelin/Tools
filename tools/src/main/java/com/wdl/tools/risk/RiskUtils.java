package com.wdl.tools.risk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.LocalServerSocket;
import android.os.Process;
import android.text.TextUtils;

import com.wdl.tools.command.CommandUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 风控检测工具类
 * <p></p>
 * 检测Android设备是否Root、是否双开、是否为模拟器操作、是否处于被调试状态、是否有改机操作等
 */
public final class RiskUtils {

    /**
     * 市面上常见多开软件的包名
     */
    private final static String[] virtualPkgs = {
            "com.bly.dkplat",//多开分身本身的包名
//            "dkplugin.pke.nnp",//多开分身克隆应用的包名会随机变换
            "com.by.chaos",//chaos引擎
            "com.pomelo.doublebox",//柚子双开
            "com.godinsec.godinsec_private_space",//x分身
            "com.boly.jyelves",//机有精灵
            "com.droid_clone.master",//克隆大师
            "com.lbe.parallel",//平行空间
            "com.excelliance.dualaid",//双开助手
            "com.lody.virtual",//VirtualXposed，VirtualApp
            "com.qihoo.magic"//360分身大师
            , "com.beike.magic",//贝壳双开
            "com.doubleopen.wxskzs",//双开小助手
    };

    // 检测是否安装改机
    private static final String XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge";
    private static final String[] EXCHANGE_MACHINE_PKG = {"de.robv.android.xposed.installer",
            "com.lody.virtual", "io.va.exposed"};


    /**
     * 判断是否root
     *
     * @return true root
     */
    public static boolean isRoot() {
        int roSecureProp = getRoSecureProp();
        if (roSecureProp == 0) {
            //  eng/userdebug版本 自带root权限
            return true;
        } else {
            return isSuExist(); // user版本，判断是否存在su文件
        }
    }

    /**
     * 检测是否存在Su文件
     *
     * @return true代表已经root
     */
    private static boolean isSuExist() {
        final String[] dirs = new String[]
                {"/system/xbin/su", "/system/bin/su", "/system/sbin/su",
                        "/sbin/su", "/vendor/bin/su", "/su/bin/su",
                        "/data/local/xbin/su", "/data/local/bin/su",
                        "/system/sd/xbin/su",
                        "/system/bin/failsafe/su",
                        "/data/local/su"
                };
        try {
            for (String dir : dirs) {
                if (new File(dir).exists()) {
                    final String result = getContent(new String[]{"ls", "-l", dir});
                    return (!TextUtils.isEmpty(result)) && (result.indexOf("root") != result.lastIndexOf("root"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取ro.secure
     *
     * @return ro.secure
     */
    private static int getRoSecureProp() {
        int roSecureProp;
        String roSecureObj = CommandUtil.getProperty("ro.secure");
        if (roSecureObj == null) {
            roSecureProp = 1;
        } else {
            if ("0".equals(roSecureObj)) {
                roSecureProp = 0;
            } else {
                roSecureProp = 1;
            }
        }
        return roSecureProp;
    }

    /**
     * 执行命令，并从获取执行结果
     *
     * @param paramArrayOfString paramArrayOfString
     * @return String
     */
    private static String getContent(String[] paramArrayOfString) {
        StringBuilder localStringBuilder = new StringBuilder();
        ProcessBuilder localProcessBuilder = new ProcessBuilder(paramArrayOfString);
        try {
            java.lang.Process localProcess = localProcessBuilder.start();
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            String str;
            while ((str = localBufferedReader.readLine()) != null) {
                localStringBuilder.append(str);
            }
            localProcess.getInputStream().close();
            localProcess.destroy();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return localStringBuilder.toString();
    }


    /**
     * 检测是否处于调试阶段
     *
     * @param context Context
     * @return true
     */
    public static boolean isDebug(Context context) {
        return checkIsDebugVersion(context) || checkIsDebuggerConnected();
    }

    /**
     * 检测APP是否为Debug版本
     *
     * @param context Context
     * @return
     */
    private static boolean checkIsDebugVersion(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 检测是否连接调试器/是否处于调试状态
     *
     * @return
     */
    public static boolean checkIsDebuggerConnected() {
        return android.os.Debug.isDebuggerConnected();
    }

    /**
     * java读取/proc/uid/status文件里TracerPid的方式来检测是否被调试
     *
     * @return
     */
    public static boolean readProcStatus() {
        try {
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/status"));
            String tracerPid = "";
            for (; ; ) {
                String str = localBufferedReader.readLine();
                if (str.contains("TracerPid")) {
                    tracerPid = str.substring(str.indexOf(":") + 1).trim();
                    break;
                }
                if (str == null) {
                    break;
                }
            }
            localBufferedReader.close();
            if ("0".equals(tracerPid)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception fuck) {
            return false;
        }
    }


    /**
     * 通过检测app私有目录，多开后的应用路径会包含多开软件的包名
     *
     * @param context
     * @return
     */
//	public static boolean checkByPrivateFilePath(Context context) {
//		String path = context.getFilesDir().getPath();
//		for (String virtualPkg : virtualPkgs) {
//			if (path.contains(virtualPkg)) {
//				return true;
//			}
//		}
//		return false;
//	}


//	public static boolean check() {
//		BufferedReader bufr = null;
//		try {
//			bufr = new BufferedReader(new FileReader("/proc/self/maps"));
//			String line;
//			while ((line = bufr.readLine()) != null) {
//				Log.e("TAG", "check: " + line);
//				for (String pkg : virtualPkgs) {
//					if (line.contains(pkg)) {
//						return true;
//					}
//				}
//			}
//		} catch (Exception ignore) {
//
//		} finally {
//			if (bufr != null) {
//				try {
//					bufr.close();
//				} catch (IOException e) {
//
//				}
//			}
//		}
//		return false;
//	}


    /**
     * 通过市面上常见的多开软件的包名与手机上安装的所有包名作比较
     *
     * @param context
     * @return 是否安装多开软件
     */
    public static boolean checkIsMultiByPkg(Context context) {
        return checkByCreateLocalServerSocket(context.getPackageName()) || isInstallApp(context, virtualPkgs);
    }


    /**
     * LocalServerSocket相同的name只能创建一次，后续的创建会抛出异常
     */
    private static volatile LocalServerSocket localServerSocket;

    /**
     * 可以防止多开
     *
     * @param uniqueMsg
     * @return
     */
    public static boolean checkByCreateLocalServerSocket(String uniqueMsg) {
        if (localServerSocket != null) {
            return false;
        }
        try {
            localServerSocket = new LocalServerSocket(uniqueMsg);
            return false;
        } catch (IOException e) {
            return true;
        }
    }


    /**
     * 判断是否安装改机软件
     *
     * @param context
     * @return
     */
    public static boolean isExchangeSoft(Context context) {
        return isXposedExistByThrow() || isInstallApp(context, EXCHANGE_MACHINE_PKG);
    }

    /**
     * 判断是否安装APP
     *
     * @param context Context
     * @param pkg     包名列表
     * @return
     */
    private static boolean isInstallApp(Context context, String[] pkg) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> pkgs = pm.getInstalledPackages(0);
            for (PackageInfo info : pkgs) {
                if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                }
                for (String virtualPkg : pkg) {
                    if (info.packageName.equals(virtualPkg)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过主动抛出异常，检查堆栈信息来判断是否存在XP框架
     *
     * @return true Xposed存在
     */
    public static boolean isXposedExistByThrow() {
        try {
            throw new Exception("isXposedExistByThrow ...... ");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().contains(XPOSED_BRIDGE))
                    return true;
            }
            return false;
        }
    }

}
