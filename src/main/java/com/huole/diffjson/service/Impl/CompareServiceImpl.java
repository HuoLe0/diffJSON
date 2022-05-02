package com.huole.diffjson.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.huole.diffjson.service.CompareService;
import com.huole.diffjson.util.CompareUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CompareServiceImpl implements CompareService {

    @Override
    @Cacheable(value = "diffResult", key = "#object")
    public JSONObject compare(JSONObject object) {
        JSONObject result = new JSONObject();
        String current = object.getString("current");
        String expected = object.getString("expected");
        if (current.equals("")){
            result.put("比对结果", "jsonA不得为空！");
            return result;
        }
        if (expected.equals("")){
            result.put("比对结果", "jsonB不得为空！");
            return result;
        }
        try {
            System.out.println("没有走缓存");
            JSONObject currentJSON = JSONObject.parseObject(current);
            JSONObject expectedJSON = JSONObject.parseObject(expected);
            JSONObject temp = CompareUtils.diffJSONObject("当前对象", currentJSON, "期望对象", expectedJSON);
            result.putAll(temp);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "请输入正确的JSON数据");
//            System.out.println(e.getMessage());
            return result;
        }
    }
}
