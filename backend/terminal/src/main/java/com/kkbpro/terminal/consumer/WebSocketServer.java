package com.kkbpro.terminal.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.lalyos.jfiglet.FigletFont;
import com.kkbpro.terminal.config.AppConfig;
import com.kkbpro.terminal.constants.enums.CharsetEnum;
import com.kkbpro.terminal.constants.enums.MessageInfoTypeRnum;
import com.kkbpro.terminal.constants.enums.ResultCodeEnum;
import com.kkbpro.terminal.controller.AdvanceController;
import com.kkbpro.terminal.pojo.dto.CooperateInfo;
import com.kkbpro.terminal.pojo.dto.EnvInfo;
import com.kkbpro.terminal.pojo.dto.MessageInfo;
import com.kkbpro.terminal.pojo.dto.PrivateKey;
import com.kkbpro.terminal.pojo.vo.FileTransInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.AESUtil;
import com.kkbpro.terminal.utils.FileUtil;
import com.kkbpro.terminal.utils.RSAUtil;
import com.kkbpro.terminal.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/socket/ssh/{ws}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static ConcurrentHashMap<String, WebSocketServer> webSocketServerMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, SSHClient> sshClientMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, SFTPClient> sftpClientMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, List<WebSocketServer>> cooperateMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, FileTransInfo>> fileTransportingMap = new ConcurrentHashMap<>();

    public static void putFileTransportingMap(String sshKey, String key, FileTransInfo fileTransInfo) {
        fileTransportingMap.get(sshKey).put(key, fileTransInfo);
        webSocketServerMap.get(sshKey).sendMessage(ResultCodeEnum.FILE_TRANSPORT_UPDATE.getDesc() ,"success", ResultCodeEnum.FILE_TRANSPORT_UPDATE.getState(), JSON.toJSONString(fileTransInfo));
    };

    public static FileTransInfo getFileTransportingMap(String sshKey, String key) {
        return fileTransportingMap.get(sshKey).get(key);
    };

    public static void removeFileTransportingMap(String sshKey, String key) {
        FileTransInfo fileTransInfo = getFileTransportingMap(sshKey, key);
        // 修改类型为已完成
        fileTransInfo.setIndex(3);
        fileTransportingMap.get(sshKey).remove(key);
        webSocketServerMap.get(sshKey).sendMessage(ResultCodeEnum.FILE_TRANSPORT_UPDATE.getDesc() ,"success", ResultCodeEnum.FILE_TRANSPORT_UPDATE.getState(), JSON.toJSONString(fileTransInfo));
    };

    private static AppConfig appConfig;

    @Getter
    private Session sessionSocket = null;

    private String secretKey;

    @Getter @Setter
    private CooperateInfo cooperateInfo;

    private Boolean cooperator = false;

    private String sshKey = null;

    private SSHClient sshClient;

    private Charset serverCharset = null;

    private net.schmizz.sshj.connection.channel.direct.Session.Shell shell = null;

    private InputStream shellInputStream;

    private OutputStream shellOutputStream;

    private Thread shellOutThread;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        WebSocketServer.appConfig = appConfig;
    }

    @OnOpen
    public void onOpen(Session sessionSocket, @PathParam("ws") String wsInfoStr) throws Exception {

        // 获取加密密钥
        wsInfoStr = AESUtil.decrypt(StringUtil.changeStr(wsInfoStr));
        JSONObject jsonObject = JSONObject.parseObject(wsInfoStr);
        this.secretKey = RSAUtil.decrypt(jsonObject.getString("secretKey"));
        // 获取连接信息
        String envInfoStr = AESUtil.decrypt(jsonObject.getString("envInfo"), this.secretKey);
        EnvInfo envInfo = JSONObject.parseObject(envInfoStr, EnvInfo.class);

        // 建立 web-socket 连接
        this.sessionSocket = sessionSocket;
        // 设置最大空闲超时
        sessionSocket.setMaxIdleTimeout(appConfig.getMaxIdleTimeout());

        // 协作
        String cooperateKey = envInfo.getCooperateKey();
        if(cooperateKey != null && !cooperateKey.isEmpty()) {
            Integer state = ResultCodeEnum.COOPERATE_KEY_INVALID.getState();
            String msg = ResultCodeEnum.COOPERATE_KEY_INVALID.getDesc();
            try {
                String[] keyInfo = AESUtil.decrypt(StringUtil.changeStrBase64(cooperateKey), AdvanceController.COOPERATE_SECRET_KEY).split("\\^");
                String cooperateId = keyInfo[0];
                String sshKey = keyInfo[1];
                WebSocketServer masterSocket = webSocketServerMap.get(sshKey);
                if(masterSocket == null || masterSocket.cooperateInfo == null || !masterSocket.cooperateInfo.getId().equals(cooperateId))
                    throw new RuntimeException();

                List<WebSocketServer> slaveSockets = cooperateMap.computeIfAbsent(sshKey, k -> new ArrayList<>());
                synchronized (slaveSockets) {
                    Integer maxHeadCount = masterSocket.cooperateInfo.getMaxHeadCount();
                    Boolean readOnly = masterSocket.cooperateInfo.getReadOnly();
                    // 成功加入协作
                    if(maxHeadCount > slaveSockets.size()) {
                        state = ResultCodeEnum.CONNECT_SUCCESS.getState();
                        msg = (readOnly ? "ReadOnly" : "Edit") + " Cooperation Success";
                        slaveSockets.add(this);
                        this.sshKey = sshKey;
                        this.cooperator = true;
                        this.serverCharset = masterSocket.serverCharset;
                        if(!readOnly) {
                            this.shell = masterSocket.shell;
                            this.shellOutputStream = masterSocket.shellOutputStream;
                        }
                        masterSocket.sendMessage(ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getDesc(),
                                "success", ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
                    }
                    else msg = "Cooperators Limit Exceeded";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.sendMessage(msg, "fail", state, null);
            return;
        }

        // 与服务器建立连接
        String host = envInfo.getServer_ip();
        int port = envInfo.getServer_port();
        String user_name = envInfo.getServer_user();
        String password = envInfo.getServer_password();
        PrivateKey privateKey = envInfo.getServer_key();
        Integer authType = envInfo.getAuthType();
        File keyFile = null;

        sshClient = new SSHClient();

        try {
            sshClient.setConnectTimeout(appConfig.getSshMaxTimeout());
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());            // 不验证主机密钥
            sshClient.connect(host,port);
            if(authType != 1) sshClient.authPassword(user_name, password);      // 使用用户名和密码进行身份验证
            else {
                // 创建本地私钥文件
                String keyPath = FileUtil.folderBasePath + "/keyProviders/" + UUID.randomUUID();
                keyFile = new File(keyPath);
                // 确保父目录存在
                if (!keyFile.getParentFile().exists()) {
                    keyFile.getParentFile().mkdirs();
                }
                // 写入私钥内容
                Files.write(Paths.get(keyFile.getAbsolutePath()), privateKey.getContent().getBytes());
                // 加载私钥
                KeyProvider keyProvider = sshClient.loadKeys(keyPath, privateKey.getPassphrase());
                // 使用私钥进行身份验证
                sshClient.authPublickey(user_name, keyProvider);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendMessage("Fail to connect remote server !","fail", ResultCodeEnum.CONNECT_FAIL.getState(), null);
            return;
        } finally {
            // 删除本地私钥文件
            if(keyFile != null) FileUtil.fileDelete(keyFile);
        }

        // 获取服务器编码格式
        try(net.schmizz.sshj.connection.channel.direct.Session sshSession = sshClient.startSession();
            net.schmizz.sshj.connection.channel.direct.Session.Command command = sshSession.exec("echo $LANG")) {
            String locale = IOUtils.readFully(command.getInputStream()).toString();
            command.join();
            serverCharset = Charset.forName(CharsetEnum.getByLinuxCharset(locale).getJavaCharset());
            sshClient.setRemoteCharset(serverCharset);
        }

        // 开启交互终端
        net.schmizz.sshj.connection.channel.direct.Session sshSession = sshClient.startSession();
        sshSession.allocateDefaultPTY();

        // 连接成功，生成key标识
        sshKey = envInfo.getLang() + "-" + serverCharset.name().replace("-","@") + "-" + UUID.randomUUID();
        this.sendMessage("SSHKey","success", ResultCodeEnum.CONNECT_SUCCESS.getState(), sshKey);
        webSocketServerMap.put(sshKey, this);
        sshClientMap.put(sshKey, sshClient);
        fileTransportingMap.put(sshKey, new ConcurrentHashMap<>());
        // 欢迎语
        this.sendMessage("Welcome","success", ResultCodeEnum.OUT_TEXT.getState(), appConfig.getWelcome() + "\r\n");
        // github源地址
        this.sendMessage("GitHub","success", ResultCodeEnum.OUT_TEXT.getState(), "source: " + appConfig.getSource() + "\r\n");
        // 生成艺术字
        String title = appConfig.getTitle();
        String titleArt = FigletFont.convertOneLine(title);

        // 分割成多行
        String[] asciiArts = titleArt.split("\n");
        for (String asciiArt : asciiArts) {
            this.sendMessage("ArtWord","success", ResultCodeEnum.OUT_TEXT.getState(), asciiArt + "\r\n");
        }

        shell = sshSession.startShell();
        shellInputStream = shell.getInputStream();
        shellOutputStream = shell.getOutputStream();
        shellOutThread = new Thread(() -> {
            byte[] buffer = new byte[8192];
            int len;
            try {
                while ((len = shellInputStream.read(buffer)) != -1) {
                    String shellOut = new String(buffer, 0, len, serverCharset);
                    this.sendMessage("ShellOut",
                            "success", ResultCodeEnum.OUT_TEXT.getState(), shellOut);
                    List<WebSocketServer> slaveSockets = cooperateMap.get(sshKey);
                    if(slaveSockets != null) {
                        for (WebSocketServer slaveSocket : slaveSockets) {
                            slaveSocket.sendMessage("ShellOut",
                                    "success", ResultCodeEnum.OUT_TEXT.getState(), shellOut);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        shellOutThread.start();
    }

    @OnClose
    public void onClose() throws Exception {
        if(cooperator) {
            List<WebSocketServer> slaveSockets = cooperateMap.get(sshKey);
            if(slaveSockets != null) {
                slaveSockets.remove(this);
                WebSocketServer masterSocket = webSocketServerMap.get(sshKey);
                if(masterSocket != null) {
                    masterSocket.sendMessage(ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getDesc(),
                            "success", ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
                }
            }
            return;
        }
        // 删除临时文件
        String key = sshKey;
        Thread deleteTmpFileThread = new Thread(() -> {
            // 延时2s执行，确保文件上传已经结束
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 临时文件根文件夹
            File temporaryRootFolder = new File(FileUtil.folderBasePath);
            File[] files = temporaryRootFolder.listFiles();
            if(files == null || files.length == 0) return;
            for (File file : files) {
                // 判断是否是本次ssh对应的临时文件夹
                if (file.isDirectory() && StringUtil.isPrefix(key, file.getName())) {
                    // 忽略正在进行文件上传的文件夹
                    if(getFileTransportingMap(key, file.getName().substring(key.length() + 1)) == null)
                        FileUtil.fileDelete(file);
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
        if(webSocketServerMap.get(key) != null && webSocketServerMap.get(key).sessionSocket != null)
            webSocketServerMap.get(key).sessionSocket.close();
        webSocketServerMap.remove(key);
        cooperateMap.remove(key);
        sessionSocket = null;
        if(fileTransportingMap.get(key) == null || fileTransportingMap.get(key).isEmpty()) {
            fileTransportingMap.remove(key);
            if(sftpClientMap.get(key) != null)
                sftpClientMap.get(key).close();
            sftpClientMap.remove(key);
            if(sshClientMap.get(key) != null)
                sshClientMap.get(key).close();
            sshClientMap.remove(key);
            sshClient = null;
            sshKey = null;
        }
    }

    // 从Client接收消息
    @OnMessage
    public void onMessage(String message) throws Exception {
        if(shell == null || shellOutputStream == null) return;

        message = AESUtil.decrypt(message, this.secretKey);
        MessageInfo messageInfo = JSONObject.parseObject(message, MessageInfo.class);

        // 改变虚拟终端大小
        if(MessageInfoTypeRnum.SIZE_CHANGE.getState().equals(messageInfo.getType())) {
            shell.changeWindowDimensions(messageInfo.getCols(),messageInfo.getRows(),0,0);
        }

        // 文本命令
        if(MessageInfoTypeRnum.USER_TEXT.getState().equals(messageInfo.getType())) {
            shellOutputStream.write(messageInfo.getContent().getBytes(serverCharset));
            shellOutputStream.flush();
        }
        // 心跳续约
        if(MessageInfoTypeRnum.HEART_BEAT.getState().equals(messageInfo.getType())) {
            shellOutputStream.write("".getBytes(StandardCharsets.UTF_8));
            shellOutputStream.flush();
            // 更新协作者数量
            List<WebSocketServer> slaveSockets = cooperateMap.get(sshKey);
            if(slaveSockets != null) {
                this.sendMessage(ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getDesc(),
                        "success", ResultCodeEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
            }
        }

    }

    @OnError
    public void onError(Session sessionSocket,Throwable error) {
        error.printStackTrace();
    }

    // 向Client发送信息
    public void sendMessage(String message, String type, Integer code, String data) {

        synchronized (this.sessionSocket) {
            try {
                // 加密数据
                if(data != null) data = AESUtil.encrypt(data, this.secretKey);
                Result result;
                if("success".equals(type)) result = Result.successStr(code,message,data);
                else if("fail".equals(type)) result = Result.fail(code,message);
                else result = Result.error(code,message);
                this.sessionSocket.getBasicRemote().sendText(JSON.toJSONString(result));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}