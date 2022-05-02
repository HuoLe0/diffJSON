package com.huole.diffjson.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

import java.util.*;

public class CompareUtils {

    /**
     * JSON格式比对，值不能为空,且key需要存在
     * @param currentEnv 当前环境
     * @param current 当前对象
     * @param expectedEnv 期望环境
     * @param expected 期望对象
     * @return 比对结果
     */
    public static JSONObject diffJSONObject(String currentEnv, Object current, String expectedEnv, Object expected){
        JSONObject jsonDiff = new JSONObject();
        JSONObject currentPathJSON = getPathJSON(JSONObject.parseObject(current.toString()));
        jsonDiff = diffJsonArray(currentEnv, current, expectedEnv, expected, currentPathJSON, jsonDiff);
        if (jsonDiff.size() < 1){
            jsonDiff.put("比对结果", "完全一致");
        }
        return jsonDiff;
    }

    /**
     * JSON格式比对，值不能为空,且key需要存在
     * @param currentEnv 当前环境
     * @param current 当前对象
     * @param expectedEnv 期望环境
     * @param expected 期望对象
     * @param currentPathJSON 当前带路径的json
     * @return 比对结果
     */
    public static JSONObject diffJsonArray(String currentEnv, Object current,
                                           String expectedEnv, Object expected,
                                           JSONObject currentPathJSON, JSONObject jsonDiff){
        /*如果是JSONObject**/
        if(isJsonObject(expected)) {
            diffJson(currentEnv, current, expectedEnv, expected, currentPathJSON, jsonDiff);
        }
        /*如果是JSONArray**/
        if(isJsonArray(expected)){
            JSONArray expectArray = JSONArray.parseArray(expected.toString());
            JSONArray currentArray = JSONArray.parseArray(current.toString());
            expectArray = sortJSONArray(expectArray);
            currentArray = sortJSONArray(currentArray);
            if(expectArray.size() != currentArray.size()){
                JSONObject tempJSON = new JSONObject();
                tempJSON.put(currentEnv+"Value", "length:" + currentArray.size());
                tempJSON.put(expectedEnv+"Value", "length:" + expectArray.size());
                jsonDiff.put("Length",tempJSON);
            }
            if(expectArray.size() > 0){
                for (int i = 0; i < expectArray.size(); i++) {
                    Object expectIndexValue = expectArray.get(i);
                    Object currentIndexValue = currentArray.get(i);
                    if (expectIndexValue != null && currentIndexValue != null) {
                        if (isJsonArray(expectIndexValue)) {
                            JSONObject getResultJSON;
                            getResultJSON = diffJsonArray(currentEnv, currentIndexValue, expectedEnv, expectIndexValue, currentPathJSON, jsonDiff);
                            if (getResultJSON != null) {
                                jsonDiff.putAll(getResultJSON);
                            }
                        }else {
                            JSONObject getResultJSON;
                            getResultJSON = diffJson(currentEnv, currentIndexValue, expectedEnv, expectIndexValue, currentPathJSON, jsonDiff);
                            if (getResultJSON != null) {
                                jsonDiff.putAll(getResultJSON);
                            }
                        }
                    }
                }
            }
        }
        return jsonDiff;
    }

    /**
     * JSON格式比对，值不能为空,且key需要存在
     * @param currentEnv 当前环境
     * @param current 当前对象
     * @param expectedEnv 期望环境
     * @param expected 期望对象
     * @param currentPathJSON 当前带路径的json
     * @return 比对结果
     */
    public static JSONObject diffJson(String currentEnv, Object current,
                                      String expectedEnv, Object expected,
                                      JSONObject currentPathJSON, JSONObject jsonDiff){

        JSONObject expectedJSON =JSONObject.parseObject(expected.toString());
        JSONObject currentJSON = JSONObject.parseObject(current.toString());

        Iterator<String> iterator = expectedJSON.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object expectedValue = expectedJSON.get(key);
            if (!currentJSON.containsKey(key)) {
                JSONObject tempJSON = new JSONObject();
                tempJSON.put(currentEnv+"Value", "不存在" + key);
                tempJSON.put(expectedEnv+"Value", expectedValue);
                jsonDiff.put(key, tempJSON);
            }

            if (currentJSON.containsKey(key)) {
                Object currentValue = currentJSON.get(key);
                if (expectedValue != null && currentValue == null || expectedValue.toString() != "null" && currentValue.toString() == "null") {
                    JSONObject tempJSON = new JSONObject();
                    tempJSON.put(currentEnv+"Value", "null");
                    tempJSON.put(expectedEnv+"Value", expectedValue);
                    jsonDiff.put(key, tempJSON);
                }
                if (expectedValue != null && currentValue != null) {
                    if (isJsonObject(expectedValue) || isJsonArray(expectedValue)) {
                        JSONObject getResultJSON;
                        getResultJSON = diffJsonArray(currentEnv, currentValue, expectedEnv, expectedValue, currentPathJSON, jsonDiff);
                        if (getResultJSON != null) {
                            jsonDiff.putAll(getResultJSON);
                        }
                    }
                    else if (!expectedValue.equals(currentValue)){
                        JSONObject tempJSON = new JSONObject();
                        tempJSON.put(currentEnv+"Value", currentValue);
                        tempJSON.put(expectedEnv+"Value", expectedValue);
                        jsonDiff.put(getPath(currentValue, currentPathJSON), tempJSON);
                    }
                }
            }
        }
        return jsonDiff;
    }

    /**
     * 对JSONArray进行排序
     * @param jsonArray 待排序
     * @return 排序后的
     * @throws JSONException 异常
     */
    public static JSONArray sortJSONArray(JSONArray jsonArray) throws JSONException{
        if (jsonArray != null){
            /*转换成String进行排序**/
            ArrayList<String> arrayFroSort = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++){
                if (isJsonObject(jsonArray.get(i))){
                    arrayFroSort.add(jsonArray.get(i).toString());
                }
            }
            Collections.sort(arrayFroSort);
            /*返回结果，重构JSONArray**/
            JSONArray resultArray = new JSONArray();
            for (int i = 0; i < arrayFroSort.size(); i++){
                resultArray.add(JSONObject.parseObject(arrayFroSort.get(i)));
            }
            return resultArray;
        }
        return null;
    }

    /**
     * 获取JSON字段详细路径
     * @param jsonObject json
     * @return 包含字段路径的json
     */
    public static JSONObject getPathJSON(JSONObject jsonObject){
        Map<String, Object> map = JSONPath.paths(jsonObject);
        JSONObject json = new JSONObject();
        for (String key : map.keySet()){
            Object value = map.get(key);
            if (!getTypeValue(value).equals("null")){
                String[] list = key.split("/");
                StringBuilder sb = new StringBuilder();
                for (String s : list){
                    if (s.equals("")){
                        sb.append(s);
                    }
                    else if (Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(s.length()-1))){
                        sb.append("[").append(s).append("]");
                    }
                    else {
                        sb.append(".").append(s);
                    }
                }
                json.put(sb.substring(1), value);
                sb.setLength(0);
            }
        }
        return json;
    }

    /**
     * 查找完整路径key
     * @param value value
     * @param pathJSON 包含路径的json
     * @return key
     */
    public static String getPath(Object value, JSONObject pathJSON){
        if (!pathJSON.containsValue(value)){
            return "不存在";
        }
        for (Map.Entry<String, Object> entry : pathJSON.entrySet()){
            if (entry.getValue().equals(value)){
                return entry.getKey();
            }
        }
        return "不存在";
    }

    /**
     * 返回当前数据类型
     * @param source 源
     * @return 类型
     */
    public static String getTypeValue(Object source){
        if(source instanceof String){
            return "String";
        }
        if(source instanceof Integer){
            return "Integer";
        }
        if(source instanceof Float){
            return "Float";
        }
        if(source instanceof Long){
            return "Long";
        }
        if(source instanceof Double){
            return "Double";
        }
        if(source instanceof Date){
            return "Date";
        }
        if(source instanceof Boolean){
            return "Boolean";
        }
        return "null";
    }

    /**
     * 判断是否为JSONObject
     * @param value value
     * @return 是否
     */
    public static boolean isJsonObject(Object value){
        try{
            if(value instanceof JSONObject) {
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 判断是否为JSONArray
     * @param value value
     * @return 是否
     */
    public static boolean isJsonArray(Object value){
        try{

            if(value instanceof JSONArray){
                return true;
            }else {
                return false;
            }

        }catch (Exception e){
            return false;
        }
    }

    public static void main(String[] args) {
        String str1 = "{\n" +
                "    \"提示\":\"请输入需要比对的JSON\"\n" +
                "}";
        String str2 = "{\n" +
                "\n" +
                "}";
//        String str1 = "{\"status\":2021,\"msg\":[{\"msg1\":\"狗东西1，今天您已经领取过，明天可以继续领取哦！\"}, {\"msg2\":\"狗东西2，今天您已经领取过，明天可以继续领取哦！\"}],\"res\":{\"remainCouponNum\":\"5\",\"userId\":\"1231232是13222\"}}";
        JSONObject jsonObject1 = JSONObject.parseObject(str1);
//        String str2 = "{\"status\":201,\"msg\":[{\"msg2\":\"1今天您已经领取过，明天可以继续领取哦！\"}, {\"msg1\":\"2今天您已经领取过，明天可以继续领取哦！\"}],,\"res\":{\"remainCouponNum\":\"5\",\"userId\":\"123123213222\"}}";
        JSONObject jsonObject2 = JSONObject.parseObject(str2);

        System.out.println("核对结果: " + diffJSONObject("str1", jsonObject1, "str2", jsonObject2).toString());
    }
}

