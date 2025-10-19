package com.kkbpro.terminal.controller;

import com.alibaba.fastjson.JSONObject;
import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.config.AppConfig;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.ResultCodeEnum;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.RSAUtil;
import com.kkbpro.terminal.utils.SessionUtil;
import com.kkbpro.terminal.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 访问权限类
 */
@RestController
@RequestMapping(Constant.API_PREFIX + "/access")
public class AccessController {

    @Autowired
    private AppConfig appConfig;

    @Log
    @PostMapping("/login")
    public Result login(String password) throws Exception {
        // 解密密码
        password = RSAUtil.decrypt(password);
        Map<String, Object> map = new HashMap<>();
        String responseKey = (String) SessionUtil.getAttribute(Constant.LOGIN_SESSION);
        if (StringUtil.isEmpty(responseKey)) {
            if (StringUtil.isEmpty(appConfig.getPassword()) || appConfig.getPassword().equals(password)) {
                responseKey = StringUtil.generateRandomString(16);
                SessionUtil.setAttribute(Constant.LOGIN_SESSION, responseKey);
            }
            else return Result.error(ResultCodeEnum.PASSWORD_INCORRECT.getState(), "密码错误");
        }
        map.put("responseKey", responseKey);

        return Result.success(JSONObject.toJSONString(map));
    }

}