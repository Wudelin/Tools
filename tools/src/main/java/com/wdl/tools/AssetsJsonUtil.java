package com.wdl.tools;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: wdl
 * @date: 2021/1/6
 * <p>
 * 获取Assets资源文件
 */
public class AssetsJsonUtil {

    public static JSONObject getJSONObject(String fileName, Context context) {
        try {
            return new JSONObject(getJson(fileName, context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getJson(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
