package com.kkbpro.terminal.controller;

import com.alibaba.fastjson.JSONObject;
import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.config.AppConfig;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.pojo.vo.OSInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统信息接口类
 **/

@RestController
@RequestMapping("/api")
public class SystemController {

    public static String serverOS = "Linux";

    private static volatile Thread monitor;

    private static final ConcurrentHashMap<String, Long> windowActiveMap = new ConcurrentHashMap<>();

    @Autowired
    private AppConfig appConfig;


    /**
     * 新建窗口初始化
     */
    @Log
    @PostMapping("/init")
    public Result init(@RequestHeader("User-Agent") String userAgent) {
        String clientOS = getOSFromUA(userAgent);
        String windowId = UUID.randomUUID().toString();
        OSInfo osInfo = new OSInfo(serverOS, clientOS, windowId);
        // PC端，Windows和Mac
        if (!"Linux".equals(serverOS) && appConfig.getPcWindowTag()) {
            windowActiveMap.put(windowId, new Date().getTime());
            startMonitor();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("osInfo", osInfo);
        map.put("storageKey", appConfig.getStorageKey());
        map.put("socketKey", WebSocketServer.SOCKET_SECRET_KEY);
        map.put("publicKey", RSAUtil.PUBLIC_KEY);

        String responseKey = (String) SessionUtil.getAttribute(Constant.LOGIN_SESSION);
        if (StringUtil.isEmpty(responseKey) && StringUtil.isEmpty(appConfig.getPassword())) {
            responseKey = StringUtil.generateRandomString(16);
            SessionUtil.setAttribute(Constant.LOGIN_SESSION, responseKey);
        }
        map.put("responseKey", responseKey);

        return Result.success(JSONObject.toJSONString(map));
    }

    private String getOSFromUA(String userAgent) {
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Other";
        }
    }


    /**
     * 开启监视线程
     */
    private void startMonitor() {
        if (monitor == null) {
            synchronized (this) {
                if (monitor == null) {
                    monitor = new Thread(() -> {
                        try {
                            while (!windowActiveMap.isEmpty()) {
                                // 超过33s则移除（判定为窗口已关闭）
                                windowActiveMap.entrySet().removeIf(entry -> new Date().getTime() - entry.getValue() > 1000L * 33);
                                Thread.sleep(1000); // 每隔1秒处理一次
                            }
                            System.exit(0); // 结束进程
                        } catch (InterruptedException e) {
                            LogUtil.logException(this.getClass(), e);
                        }
                    });
                    monitor.start();
                }
            }
        }
    }


    /**
     * 窗口心跳续约
     */
    @Log
    @PostMapping ("/beat")
    public Result beat(String windowId) {
        if (!"Linux".equals(serverOS) && appConfig.getPcWindowTag() && windowId != null)
            windowActiveMap.replace(windowId, new Date().getTime());
        return Result.success("窗口心跳续约成功");
    }

    /**
     * 是否开启监视线程
     */
    public static Boolean isEnableMonitor() {
        return monitor != null;
    }

}
