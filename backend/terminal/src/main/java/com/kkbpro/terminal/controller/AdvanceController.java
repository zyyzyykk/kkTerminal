package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.constants.enums.FileBlockStateEnum;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.pojo.dto.CooperateInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.AesUtil;
import com.kkbpro.terminal.utils.StringUtil;
import net.schmizz.sshj.SSHClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 高级功能接口类
 */
@RestController
@RequestMapping("/api")
public class AdvanceController {

    public static final String COOPERATE_SECRET_KEY = "o4D1fYuVp2js9xKX";

    /**
     * 获取协作id
     */
    @GetMapping("/cooperate")
    public Result getCooperateId(String sshKey, Boolean readOnly, Integer maxHeadCount) throws Exception {
        String errorMsg = "协作Key生成失败";
        String successMsg = "协作Key生成成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        if(ssh == null || webSocketServer == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        if(webSocketServer.getCooperateInfo() != null)
            return Result.error(errorMsg);

        CooperateInfo cooperateInfo = new CooperateInfo();
        cooperateInfo.setReadOnly(readOnly);
        cooperateInfo.setMaxHeadCount(maxHeadCount);
        webSocketServer.setCooperateInfo(cooperateInfo);

        String key = StringUtil.changeBase64Str(AesUtil.aesEncrypt(sshKey, COOPERATE_SECRET_KEY));

        return Result.success(successMsg, key);
    }




}
