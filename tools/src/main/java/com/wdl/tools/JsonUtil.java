package com.wdl.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Json 工具类
 */
public final class JsonUtil {
    private JsonUtil() {

    }

    /**
     * 转换成JSON
     *
     * @param obj Object-String或者Map
     * @return JSONObject
     */
    public static JSONObject parseObject(Object obj) {
        JSONObject jsonObject = null;
        try {
            if (obj != null) {
                if (obj instanceof String) {
                    jsonObject = new JSONObject((String) obj);
                } else if (obj instanceof Map) {
                    jsonObject = new JSONObject((Map) obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 将string json 转化成 map
     *
     * @param strJson json格式字符串
     * @return Map<String, Object>
     */
    public static Map<String, Object> jsonStringToMap(String strJson) {
        JSONObject obj = parseObject(strJson);
        try {
            if (obj != null) {
                return jsonObjectToMap(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json转map
     *
     * @param json JSONObject
     * @return Map
     */
    public static Map<String, Object> jsonObjectToMap(JSONObject json) {
        final Map<String, Object> result = new HashMap<>();
        if (null != json) {
            try {
                final Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = json.get(key);
                    if (value instanceof JSONObject) {
                        Map<String, Object> valMap = jsonObjectToMap((JSONObject) value);
                        result.put(key, valMap);
                    } else if (value instanceof JSONArray) {
                        JSONArray ja = (JSONArray) value;
                        result.put(key, jsonArrayToList(ja));
                    } else {
                        result.put(key, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return result;
    }


    /**
     * jsonArray转List
     *
     * @param jsonArray JSONArray
     * @return List
     */
    public static List<Object> jsonArrayToList(JSONArray jsonArray) {
        final List<Object> result = new ArrayList<>();
        if (null != jsonArray) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object val = jsonArray.get(i);
                    if (val != null) {
                        if (val instanceof JSONObject) {
                            Map<String, Object> map = jsonObjectToMap((JSONObject) val);
                            result.add(map);
                        } else if (val instanceof JSONArray) {
                            result.add(jsonArrayToList((JSONArray) val));
                        } else {
                            result.add(val);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
