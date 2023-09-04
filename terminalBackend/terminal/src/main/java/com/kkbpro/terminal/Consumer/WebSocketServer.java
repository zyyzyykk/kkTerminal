package com.kkbpro.terminal.Consumer;

import com.alibaba.fastjson.JSON;
import com.github.lalyos.jfiglet.FigletFont;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.kkbpro.terminal.Config.AppConfig;
import com.kkbpro.terminal.Constants.Constants;
import com.kkbpro.terminal.Constants.Enum.ResultCodeEnum;
import com.kkbpro.terminal.Result.Result;
import com.kkbpro.terminal.Utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{user_name}/{password}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static ConcurrentHashMap<String, Session> onlineSocket = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, com.jcraft.jsch.Session> onlineSSH = new ConcurrentHashMap<>();

    private static AppConfig appConfig;

    private String key = null;

    private Session sessionSocket = null;

    @Autowired
    public void setUserMapper(AppConfig appConfig) {
        WebSocketServer.appConfig = appConfig;
    }

    @OnOpen
    public void onOpen(Session sessionSocket, @PathParam("user_name") String user_name,
                       @PathParam("password") String password) throws IOException {

        // 建立 web-socket 连接
        this.sessionSocket = sessionSocket;

        // 与服务器建立连接
        String host = appConfig.getServerIP();
        int port = Integer.parseInt(appConfig.getServerPort());

        com.jcraft.jsch.Session sessionSSH = null;

        try {
            JSch jsch = new JSch(); // 创建JSch对象
            sessionSSH = jsch.getSession(user_name, host, port);
            sessionSSH.setPassword(password); // 设置会话密码
            sessionSSH.setConfig("StrictHostKeyChecking", "no"); // 设置会话配置，取消主机密钥检查
            sessionSSH.connect(); // 连接到远程服务器
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return;
        }

        // 连接成功
        this.key = StringUtil.getRandomStr(Constants.SSH_RANDOM_KEY_LEN);
        onlineSSH.put(this.key,sessionSSH);
        onlineSocket.put(this.key,sessionSocket);
        // 欢迎语
        sendMessage(sessionSocket,"Welcome to kk Server","success", ResultCodeEnum.CONTENT_SHOW.getState());
        // 生成艺术字
        String title = appConfig.getTitle();
        String titleArt = FigletFont.convertOneLine(title);

        // 分割成多行
        String[] asciiArts = titleArt.split("\n");
        for (String asciiArt : asciiArts) {
            sendMessage(sessionSocket,asciiArt,"success", ResultCodeEnum.CONTENT_SHOW.getState());
        }
        sendMessage(sessionSocket,getPrefix(sessionSSH),"success", ResultCodeEnum.LINE_PREFIX.getState());
    }


    @OnClose
    public void onClose() throws IOException {
        if(this.key != null) {
            onlineSocket.remove(key);
            onlineSSH.remove(key);
            this.key = null;
        }
    }

    // 从 Client 接收消息
    @OnMessage
    public void onMessage(String message) throws IOException {


    }

    @OnError
    public void onError(Session sessionSocket,Throwable error) {
        error.printStackTrace();
    }

    // 获取前缀 [root@ECS931 kk4]#
    public String getPrefix(com.jcraft.jsch.Session sessionSSH) {
        String prefix = "[";
        // 获取用户名
        try {
            List<String> infoList = doCommand(sessionSSH, "whoami");
            if(infoList.size() == 0) throw new RuntimeException();
            prefix += infoList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return null;
        }
        // 获取主机名
        try {
            List<String> infoList = doCommand(sessionSSH, "hostname");
            if(infoList.size() == 0) throw new RuntimeException();
            prefix += "@" + infoList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return null;
        }
        // 获取目录
        try {
            List<String> infoList = doCommand(sessionSSH, "pwd");
            if(infoList.size() == 0) throw new RuntimeException();
            String dir = infoList.get(0);
            prefix += StringUtil.getEndStr(dir) + "]# ";
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return null;
        }

        return prefix;
    }

    // 在服务器执行输入的命令
    public List<String> doCommand(com.jcraft.jsch.Session sessionSSH, String command) throws JSchException, IOException {
        Channel channel = sessionSSH.openChannel("exec"); // 打开exec通道
        String environment = "export LANG=zh_CN.UTF-8; ";
        ((ChannelExec) channel).setCommand(environment + command); // 设置要执行的命令
        channel.setInputStream(null); // 设置通道输入流为空
        ((ChannelExec) channel).setErrStream(System.err); // 设置通道错误输出流为标准错误输出流

        InputStream in = channel.getInputStream(); // 获取通道输入流
        channel.connect(); // 连接到远程服务器并执行命令

        List<String> cmdInfoList = new ArrayList<>();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                cmdInfoList.add(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                break;
            }
            try {
                Thread.sleep(10); // 等待命令执行完成
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        channel.disconnect(); // 断开通道连接

        return cmdInfoList;
    }

    // 向 Client 发送信息
    public void sendMessage(Session sessionSocket, String message, String type, Integer code) {
        synchronized (sessionSocket) {
            try {
                Result result = null;
                if("success".equals(type)) {
                    result = Result.setSuccess(code,message,null);
                }
                else if("fail".equals(type)) {
                    result = Result.setFail(code,message);
                }
                else {
                    result = Result.setError(code,message);
                }
                sessionSocket.getBasicRemote().sendText(JSON.toJSONString(result));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}
