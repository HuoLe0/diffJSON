package com.huole.diffjson.controller;

import com.alibaba.fastjson.JSONObject;
import com.huole.diffjson.service.CompareService;
import com.huole.diffjson.util.CompareUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
public class CompareController {

    @Autowired
    private CompareService compareService;

    @PostMapping("diff")
    public JSONObject compare(@RequestBody JSONObject object, HttpServletRequest request, HttpServletResponse response){
        String origin = request.getHeader("Origin");
        if (origin == null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
        } else if (request.getMethod().equals("OPTIONS")) {
            response.addHeader("Access-Control-Allow-Origin", origin);
        }
        return compareService.compare(object);
    }
}
