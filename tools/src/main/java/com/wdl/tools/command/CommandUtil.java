package com.wdl.tools.command;

import android.annotation.SuppressLint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * 命令行工具类
 */
public class CommandUtil {
	private CommandUtil() {
	}

	/**
	 * 根据属性名获取属性值
	 *
	 * @param propName propName 属性名
	 * @return
	 */
	@SuppressLint("PrivateApi")
	public static String getProperty(String propName) {
		String propValue = null;
		Object roSecureObj = null;
		try {
			roSecureObj = Class.forName("android.os.SystemProperties")
					.getMethod("get", String.class)
					.invoke(null, propName);
			if (roSecureObj != null) {
				propValue = (String) roSecureObj;
			}
		} catch (Exception e) {
			e.printStackTrace();
			propValue = null;
		}
		return propValue;
	}

	/**
	 *
	 * @param command String
	 * @return
	 */
	public static String exec(String command) {
		BufferedOutputStream bufferedOutputStream = null;
		BufferedInputStream bufferedInputStream = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("sh");
			bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

			bufferedInputStream = new BufferedInputStream(process.getInputStream());
			bufferedOutputStream.write(command.getBytes());
			bufferedOutputStream.write('\n');
			bufferedOutputStream.flush();
			bufferedOutputStream.close();

			process.waitFor();

			String outputStr = getStrFromBufferInputSteam(bufferedInputStream);
			return outputStr;
		} catch (Exception e) {
			return null;
		} finally {
			if (bufferedOutputStream != null) {
				try {
					bufferedOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (process != null) {
				process.destroy();
			}
		}
	}

	private static String getStrFromBufferInputSteam(BufferedInputStream bufferedInputStream) {
		if (null == bufferedInputStream) {
			return "";
		}
		int BUFFER_SIZE = 512;
		byte[] buffer = new byte[BUFFER_SIZE];
		StringBuilder result = new StringBuilder();
		try {
			while (true) {
				int read = bufferedInputStream.read(buffer);
				if (read > 0) {
					result.append(new String(buffer, 0, read));
				}
				if (read < BUFFER_SIZE) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
}
