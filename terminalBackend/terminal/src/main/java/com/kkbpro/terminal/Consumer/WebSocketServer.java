package com.kkbpro.terminal.Consumer;

import com.alibaba.fastjson.JSON;
import com.github.lalyos.jfiglet.FigletFont;
import com.kkbpro.terminal.Config.AppConfig;
import com.kkbpro.terminal.Constants.Constants;
import com.kkbpro.terminal.Constants.Enum.FrontSocketEnum;
import com.kkbpro.terminal.Constants.Enum.ResultCodeEnum;
import com.kkbpro.terminal.Pojo.FrontSocketInfo;
import com.kkbpro.terminal.Result.Result;
import com.kkbpro.terminal.Utils.BASE64Util;
import com.kkbpro.terminal.Utils.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint("/websocket/{user_name}/{password}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static ConcurrentHashMap<String, Session> onlineSocket = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, SSHClient> onlineSSH = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, net.schmizz.sshj.connection.channel.direct.Session.Shell> onlineShell = new ConcurrentHashMap<>();

    private static AppConfig appConfig;

    private String key = null;

    private Session sessionSocket = null;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    public void setUserMapper(AppConfig appConfig) {
        WebSocketServer.appConfig = appConfig;
    }

    @OnOpen
    public void onOpen(Session sessionSocket, @PathParam("user_name") String user_name,
                       @PathParam("password") String password) throws IOException {

        password = BASE64Util.Base64Decrypt(password);

        // 建立 web-socket 连接
        this.sessionSocket = sessionSocket;

        // 与服务器建立连接
        String host = appConfig.getServerIP();
        int port = Integer.parseInt(appConfig.getServerPort());
        net.schmizz.sshj.connection.channel.direct.Session sessionSSH = null;

        SSHClient ssh = new SSHClient();

        try {
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier()); // 不验证主机密钥
            ssh.connect(host,port);
            ssh.authPassword(user_name, password);    // 使用用户名和密码进行身份验证

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return;
        }

        // 连接成功

        // 打开一个交互式 Shell 通道
        net.schmizz.sshj.connection.channel.direct.Session session = ssh.startSession();

        // 打开一个交互式Shell通道
        session.allocateDefaultPTY();
        net.schmizz.sshj.connection.channel.direct.Session.Shell shell = session.startShell();

        this.key = StringUtil.getRandomStr(Constants.SSH_RANDOM_KEY_LEN);
        onlineSSH.put(this.key,ssh);
        onlineSocket.put(this.key,sessionSocket);
        onlineShell.put(this.key,shell);
        // 欢迎语
        sendMessage(sessionSocket,"Welcome to kk Server","success", ResultCodeEnum.KK_SHOW.getState());
        // 生成艺术字
        String title = appConfig.getTitle();
        String titleArt = FigletFont.convertOneLine(title);

        // 分割成多行
        String[] asciiArts = titleArt.split("\n");
        for (String asciiArt : asciiArts) {
            sendMessage(sessionSocket,asciiArt,"success", ResultCodeEnum.KK_SHOW.getState());
        }
        sendMessage(sessionSocket,"init","success", ResultCodeEnum.GET_INIT.getState());
    }


    @OnClose
    public void onClose() throws IOException {
        System.out.println("连接断开");
        if(this.key != null) {
            onlineSocket.remove(key);
            onlineSSH.remove(key);
            this.key = null;
        }
    }

    // 从 Client 接收消息
    @OnMessage
    public void onMessage(String message) throws IOException {
        message = BASE64Util.Base64Decrypt(message);
        FrontSocketInfo frontSocketInfo = JSON.parseObject(message, FrontSocketInfo.class);

        // 获取 sessionSSH
        SSHClient ssh = onlineSSH.get(this.key);
        net.schmizz.sshj.connection.channel.direct.Session.Shell shell = onlineShell.get(this.key);
        Session sessionSocket = onlineSocket.get(this.key);

        // 文本命令
        if(FrontSocketEnum.TEXT_CMD.getState().equals(frontSocketInfo.getType())) {
            try {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doCommandShell(shell,sessionSocket,frontSocketInfo.getContent());
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
                            // 停止线程
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
                return;
            }
        }
    }

    @OnError
    public void onError(Session sessionSocket,Throwable error) {
        error.printStackTrace();
    }


    // 向 Client 发送信息
    public void sendMessage(Session sessionSocket, String message, String type, Integer code) {
        if(message == null || "".equals(message)) return;
        message = BASE64Util.Base64Encrypt(message);
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

    // Shell模式连接服务器
    public void doCommandShell(net.schmizz.sshj.connection.channel.direct.Session.Shell shell,
                               Session sessionSocket, String content) throws Exception {
        // 获取输入输出流
        InputStream stdin = shell.getInputStream();
        OutputStream stdout = shell.getOutputStream();

        // 向Shell通道发送命令
        stdout.write(content.getBytes(StandardCharsets.UTF_8));
        stdout.flush();

        // 获取命令输出流中的内容
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stdin.read(buffer)) != -1) {
            System.out.println(new String(buffer, 0, len, StandardCharsets.UTF_8));
            sendMessage(sessionSocket, new String(buffer, 0, len, StandardCharsets.UTF_8) + "\n",
                    "success", ResultCodeEnum.SHELL_SHOW.getState());
        }
    }
}