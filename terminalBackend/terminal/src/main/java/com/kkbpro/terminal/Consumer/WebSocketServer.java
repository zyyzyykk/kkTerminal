package com.kkbpro.terminal.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.lalyos.jfiglet.FigletFont;
import com.kkbpro.terminal.Config.AppConfig;
import com.kkbpro.terminal.Constants.Enum.MessageInfoTypeRnum;
import com.kkbpro.terminal.Constants.Enum.ResultCodeEnum;
import com.kkbpro.terminal.Pojo.EnvInfo;
import com.kkbpro.terminal.Pojo.MessageInfo;
import com.kkbpro.terminal.Result.Result;
import com.kkbpro.terminal.Utils.AesUtil;
import com.kkbpro.terminal.Utils.FileUtil;
import com.kkbpro.terminal.Utils.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/socket/ssh/{env}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static ConcurrentHashMap<String, SSHClient> sshClientMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, EnvInfo> envInfoMap = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<String, String> fileUploadingMap = new ConcurrentHashMap<>();
    private static AppConfig appConfig;

    private Session sessionSocket = null;

    private String sshKey = null;

    private SSHClient sshClient;

    private net.schmizz.sshj.connection.channel.direct.Session.Shell shell = null;

    private InputStream shellInputStream;

    private OutputStream shellOutputStream;

    private Thread shellOutThread;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        WebSocketServer.appConfig = appConfig;
    }

    @OnOpen
    public void onOpen(Session sessionSocket, @PathParam("env") String env) throws IOException {

        EnvInfo envInfo =
                JSONObject.parseObject(AesUtil.aesDecrypt(StringUtil.changeStr(env)),EnvInfo.class);

        // 建立 web-socket 连接
        this.sessionSocket = sessionSocket;
        // 设置最大空闲超时（上线后失效？？？）
        sessionSocket.setMaxIdleTimeout(appConfig.getMaxIdleTimeout());

        // 与服务器建立连接
        String host = envInfo.getServer_ip();
        int port = envInfo.getServer_port();
        String user_name = envInfo.getServer_user();
        String password = envInfo.getServer_password();

        sshClient = new SSHClient();

        try {
            sshClient.setConnectTimeout(appConfig.getSshMaxTimeout());
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());    // 不验证主机密钥
            sshClient.connect(host,port);
            sshClient.authPassword(user_name, password);                // 使用用户名和密码进行身份验证

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(sessionSocket,"连接服务器失败","fail", ResultCodeEnum.CONNECT_FAIL.getState());
            return;
        }

        // 连接成功，生成key标识
        sshKey = UUID.randomUUID().toString();
        sendMessage(sessionSocket, sshKey,"success", ResultCodeEnum.CONNECT_SUCCESS.getState());
        sshClientMap.put(sshKey,sshClient);
        envInfoMap.put(sshKey,envInfo);
        // 欢迎语
        sendMessage(sessionSocket, appConfig.getWelcome() + "\r\n","success", ResultCodeEnum.OUT_TEXT.getState());
        // github源地址
        sendMessage(sessionSocket, "source: " + appConfig.getSource() + "\r\n","success", ResultCodeEnum.OUT_TEXT.getState());
        // 生成艺术字
        String title = appConfig.getTitle();
        String titleArt = FigletFont.convertOneLine(title);

        // 分割成多行
        String[] asciiArts = titleArt.split("\n");
        for (String asciiArt : asciiArts) {
            sendMessage(sessionSocket,asciiArt + "\r\n","success", ResultCodeEnum.OUT_TEXT.getState());
        }


        net.schmizz.sshj.connection.channel.direct.Session sshSession = sshClient.startSession();
        sshSession.allocateDefaultPTY();

        shell = sshSession.startShell();
        shellInputStream = shell.getInputStream();
        shellOutputStream = shell.getOutputStream();
        shellOutThread = new Thread(() -> {
            byte[] buffer = new byte[8192];
            int len;
            try {
                while ((len = shellInputStream.read(buffer)) != -1) {
                    String shellOut = new String(buffer, 0, len, StandardCharsets.UTF_8);
                    System.out.println(shellOut);
                    sendMessage(sessionSocket, shellOut,
                            "success", ResultCodeEnum.OUT_TEXT.getState());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        shellOutThread.start();
    }

    @OnClose
    public void onClose() throws IOException {
        // 删除临时文件
        Thread deleteTmpFileThread = new Thread(() -> {
            // 延时2s执行
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String key = sshKey;
            // 临时文件根文件夹
            File temporaryRootFolder = new File(FileUtil.folderBasePath);
            File[] files = temporaryRootFolder.listFiles();
            for (File file : files) {
                // 判断是否是本次ssh对应的临时文件夹
                if (file.isDirectory() && StringUtil.isPrefix(key, file.getName())) {
                    // 忽略正在进行文件上传的文件夹
                    if(fileUploadingMap.get(file.getName()) == null)
                        FileUtil.tmpFloderDelete(file);
                }
            }
        });
        deleteTmpFileThread.start();
        // 释放资源
        if (shellOutThread != null && !shellOutThread.isInterrupted())
            shellOutThread.interrupt();
        if(shellOutputStream != null)
            shellOutputStream.close();
        if(shellInputStream != null)
            shellInputStream.close();
        if(shell != null)
            shell.close();
        if(sshClient != null)
            sshClient.disconnect();
        sessionSocket = null;
        if(sshKey != null) {
            sshClientMap.remove(sshKey);
            sshKey = null;
        }
    }

    // 从 Client 接收消息
    @OnMessage
    public void onMessage(String message) throws IOException {

        message = AesUtil.aesDecrypt(message);
        MessageInfo messageInfo = JSONObject.parseObject(message, MessageInfo.class);

        // 改变虚拟终端大小
        if(MessageInfoTypeRnum.SIZE_CHANGE.getState().equals(messageInfo.getType())) {
             shell.changeWindowDimensions(messageInfo.getCols(),messageInfo.getRows(),0,0);
        }

        // 文本命令
        if(MessageInfoTypeRnum.USER_TEXT.getState().equals(messageInfo.getType())) {
            shellOutputStream.write(messageInfo.getContent().getBytes(StandardCharsets.UTF_8));
            shellOutputStream.flush();
        }
        // 心跳续约
        if(MessageInfoTypeRnum.HEART_BEAT.getState().equals(messageInfo.getType())) {
            System.out.println("心跳续约");
        }

    }

    @OnError
    public void onError(Session sessionSocket,Throwable error) {
        error.printStackTrace();
    }


    // 向 Client 发送信息
    public void sendMessage(Session sessionSocket, String message, String type, Integer code) {
        if(message == null || "".equals(message)) return;
        message = AesUtil.aesEncrypt(message);
        synchronized (sessionSocket) {
            try {
                Result result = null;
                if("success".equals(type)) result = Result.setSuccess(code,message,null);
                else if("fail".equals(type)) result = Result.setFail(code,message);
                else result = Result.setError(code,message);
                sessionSocket.getBasicRemote().sendText(JSON.toJSONString(result));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}