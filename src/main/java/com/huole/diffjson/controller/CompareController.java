package com.huole.diffjson.controller;

import com.alibaba.fastjson.JSONObject;
import com.huole.diffjson.util.CompareUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
public class CompareController {

    @PostMapping("diff")
    public JSONObject compare(@RequestBody JSONObject object, HttpServletRequest request, HttpServletResponse response){
        JSONObject result = new JSONObject();
        String current = object.getString("current");
        String expected = object.getString("expected");
        String origin = request.getHeader("Origin");
        if (origin == null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
        } else if (request.getMethod().equals("OPTIONS")) {
            response.addHeader("Access-Control-Allow-Origin", origin);
        }
        try {
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
