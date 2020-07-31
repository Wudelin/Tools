package com.wdl.tools.risk;

/**
 * 检测是否为模拟器的返回类
 */
public class EmulatorResult {
	public static final int RESULT_MAYBE_EMULATOR = 0;  //可能是模拟器
	public static final int RESULT_EMULATOR = 1;        //模拟器
	public static final int RESULT_UNKNOWN = 2;         //可能是真机

	public int result;
	public String value;

	public EmulatorResult(int result, String value) {
		this.result = result;
		this.value = value;
	}
}