package com.kkbpro.terminal.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.lalyos.jfiglet.FigletFont;
import com.kkbpro.terminal.config.AppConfig;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.ResultStatusEnum;
import com.kkbpro.terminal.enums.SocketMessageEnum;
import com.kkbpro.terminal.enums.SocketSendEnum;
import com.kkbpro.terminal.controller.AdvanceController;
import com.kkbpro.terminal.pojo.dto.CooperateInfo;
import com.kkbpro.terminal.pojo.dto.EnvInfo;
import com.kkbpro.terminal.pojo.dto.MessageInfo;
import com.kkbpro.terminal.pojo.vo.FileTransInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.*;
import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.sftp.SFTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(Constant.API_PREFIX + "/socket/ssh/{ws}")  // 注意不要以'/'结尾
public class WebSocketServer {

    public static final String SOCKET_SECRET_KEY = StringUtil.generateRandomString(16);

    public static final ConcurrentHashMap<String, WebSocketServer> webSocketServerMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, List<WebSocketServer>> cooperateMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, SSHClient> sshClientMap = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, SFTPClient> sftpClientMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, FileTransInfo>> fileTransportingMap = new ConcurrentHashMap<>();

    public static void putTransportingFile(String sshKey, String key, FileTransInfo fileTransInfo) {
        if (fileTransportingMap.get(sshKey) == null) return;
        fileTransportingMap.get(sshKey).put(key, fileTransInfo);
        WebSocketServer webSocketServer = webSocketServerMap.get(sshKey);
        if (webSocketServer == null) return;
        webSocketServer.sendMessage(SocketSendEnum.FILE_TRANSPORT_UPDATE.getDesc(), ResultStatusEnum.SUCCESS.getStatus(),
                SocketSendEnum.FILE_TRANSPORT_UPDATE.getState(), JSON.toJSONString(fileTransInfo));
    };

    public static FileTransInfo getTransportingFile(String sshKey, String key) {
        if (fileTransportingMap.get(sshKey) == null) return null;
        return fileTransportingMap.get(sshKey).get(key);
    };

    public static void removeTransportingFile(String sshKey, String key) {
        FileTransInfo fileTransInfo = getTransportingFile(sshKey, key);
        if (fileTransInfo == null) return;
        // 修改类型为 已完成
        fileTransInfo.setIndex(3);
        fileTransportingMap.get(sshKey).remove(key);
        WebSocketServer webSocketServer = webSocketServerMap.get(sshKey);
        if (webSocketServer == null) return;
        webSocketServer.sendMessage(SocketSendEnum.FILE_TRANSPORT_UPDATE.getDesc(), ResultStatusEnum.SUCCESS.getStatus(),
                SocketSendEnum.FILE_TRANSPORT_UPDATE.getState(), JSON.toJSONString(fileTransInfo));
    };

    private static AppConfig appConfig;

    @Getter
    private Session sessionSocket;

    private String secretKey;

    @Getter @Setter
    private CooperateInfo cooperateInfo;

    private Boolean cooperator = false;

    private String sshKey;

    private Charset serverCharset;

    @Getter
    private SSHClient sshClient;

    @Getter
    private SFTPClient sftpClient;

    private net.schmizz.sshj.connection.channel.direct.Session.Shell shell;

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
        wsInfoStr = AESUtil.decrypt(StringUtil.changeStr(wsInfoStr), SOCKET_SECRET_KEY);
        JSONObject jsonObject = JSONObject.parseObject(wsInfoStr);
        this.secretKey = RSAUtil.decrypt(jsonObject.getString("secretKey"));
        // 获取连接信息
        String envInfoStr = AESUtil.decrypt(jsonObject.getString("envInfo"), this.secretKey);
        EnvInfo envInfo = JSONObject.parseObject(envInfoStr, EnvInfo.class);

        // 建立WebSocket连接
        this.sessionSocket = sessionSocket;

        // 协作
        String cooperateKey = envInfo.getCooperateKey();
        if (!StringUtil.isEmpty(cooperateKey)) {
            Integer state = SocketSendEnum.COOPERATE_KEY_INVALID.getState();
            String cooperateTip = SocketSendEnum.COOPERATE_KEY_INVALID.getDesc();
            try {
                String[] keyInfo = AESUtil.decrypt(StringUtil.changeStrToBase64(cooperateKey), AdvanceController.COOPERATE_SECRET_KEY).split("\\^");
                String cooperateId = keyInfo[0];
                String sshKey = keyInfo[1];
                WebSocketServer masterSocket = webSocketServerMap.get(sshKey);
                if (masterSocket == null || masterSocket.cooperateInfo == null || !masterSocket.cooperateInfo.getId().equals(cooperateId))
                    throw new RuntimeException();

                List<WebSocketServer> slaveSockets = cooperateMap.computeIfAbsent(sshKey, k -> new ArrayList<>());
                synchronized (slaveSockets) {
                    Integer maxCapacity = masterSocket.cooperateInfo.getMaxCapacity();
                    Boolean readOnly = masterSocket.cooperateInfo.getReadOnly();
                    // 成功加入协作
                    if (maxCapacity > slaveSockets.size()) {
                        state = SocketSendEnum.CONNECT_SUCCESS.getState();
                        cooperateTip = (readOnly ? "Read-Only" : "Editable") + " Cooperation Success";
                        slaveSockets.add(this);
                        this.sshKey = sshKey;
                        this.cooperator = true;
                        this.serverCharset = masterSocket.serverCharset;
                        if (!readOnly) {
                            this.shell = masterSocket.shell;
                            this.shellOutputStream = masterSocket.shellOutputStream;
                        }
                        masterSocket.sendMessage(SocketSendEnum.COOPERATE_NUMBER_UPDATE.getDesc(), ResultStatusEnum.SUCCESS.getStatus(),
                                SocketSendEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
                    }
                    else cooperateTip = "Capacity Limit Exceeded";
                }
            } catch (Exception e) {
                LogUtil.logException(this.getClass(), e);
            }
            this.sendMessage(cooperateTip, ResultStatusEnum.WARNING.getStatus(), state, null);
            return;
        }

        // 建立交互SSH连接
        try {
            this.sshClient = SSHUtil.connectHost(envInfo);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            this.sendMessage("Fail to connect remote server !", ResultStatusEnum.WARNING.getStatus(),
                    SocketSendEnum.CONNECT_FAIL.getState(), null);
            return;
        };

        // 获取服务器编码格式
        try(net.schmizz.sshj.connection.channel.direct.Session sshSession = this.sshClient.startSession();
            net.schmizz.sshj.connection.channel.direct.Session.Command command = sshSession.exec("echo -n $(locale charmap)")) {
            String charsetName = IOUtils.readFully(command.getInputStream()).toString();
            command.join();
            this.serverCharset = Charset.forName(charsetName);
            this.sshClient.setRemoteCharset(this.serverCharset);
        }

        // 开启交互终端和SFTP
        net.schmizz.sshj.connection.channel.direct.Session sshSession = this.sshClient.startSession();
        sshSession.allocateDefaultPTY();
        this.sftpClient = this.sshClient.newSFTPClient();

        // 生成唯一标识
        this.sshKey = envInfo.getLang() + "-" + this.serverCharset.name().replace("-","@") + "-" + UUID.randomUUID();
        this.sendMessage("SSHKey", ResultStatusEnum.SUCCESS.getStatus(),
                SocketSendEnum.CONNECT_SUCCESS.getState(), this.sshKey);
        webSocketServerMap.put(this.sshKey, this);
        fileTransportingMap.put(this.sshKey, new ConcurrentHashMap<>());
        // 欢迎语
        this.sendMessage("Welcome", ResultStatusEnum.SUCCESS.getStatus(),
                SocketSendEnum.OUT_TEXT.getState(), appConfig.getWelcome() + "\r\n");
        // github源地址
        this.sendMessage("GitHub", ResultStatusEnum.SUCCESS.getStatus(),
                SocketSendEnum.OUT_TEXT.getState(), appConfig.getSource() + "\r\n");
        // 生成横幅艺术字
        String banner = appConfig.getBanner();
        String bannerArt = FigletFont.convertOneLine(banner);

        // 分割成多行
        String[] asciiArts = bannerArt.split("\n");
        for (String asciiArt : asciiArts) {
            this.sendMessage("BannerArtLine", ResultStatusEnum.SUCCESS.getStatus(),
                    SocketSendEnum.OUT_TEXT.getState(), asciiArt + "\r\n");
        }

        this.shell = sshSession.startShell();
        this.shellInputStream = this.shell.getInputStream();
        this.shellOutputStream = this.shell.getOutputStream();
        this.shellOutThread = new Thread(() -> {
            byte[] buffer = new byte[Constant.BUFFER_SIZE];
            int len;
            try {
                while ((len = this.shellInputStream.read(buffer)) != -1) {
                    String shellOut = new String(buffer, 0, len, this.serverCharset);
                    this.sendMessage("ShellOut", ResultStatusEnum.SUCCESS.getStatus(),
                            SocketSendEnum.OUT_TEXT.getState(), shellOut);
                    List<WebSocketServer> slaveSockets = cooperateMap.get(this.sshKey);
                    if (slaveSockets != null) {
                        for (WebSocketServer slaveSocket : slaveSockets) {
                            slaveSocket.sendMessage("ShellOut", ResultStatusEnum.SUCCESS.getStatus(),
                                    SocketSendEnum.OUT_TEXT.getState(), shellOut);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.logException(this.getClass(), e);
            }
        });
        this.shellOutThread.start();
        // 建立传输SSH连接（异步）
        new Thread(() -> {
            try {
                SSHClient ssh = SSHUtil.connectHost(envInfo);
                SFTPClient sftp = ssh.newSFTPClient();
                sshClientMap.put(this.sshKey, ssh);
                sftpClientMap.put(sshKey, sftp);
            } catch (Exception e) {
                LogUtil.logException(this.getClass(), e);
            };
        }).start();
    }

    @OnClose
    public void onClose() {
        // 释放协作资源
        if (this.cooperator) {
            List<WebSocketServer> slaveSockets = cooperateMap.get(this.sshKey);
            if (slaveSockets != null) {
                slaveSockets.remove(this);
                WebSocketServer masterSocket = webSocketServerMap.get(this.sshKey);
                if (masterSocket != null) {
                    masterSocket.sendMessage(SocketSendEnum.COOPERATE_NUMBER_UPDATE.getDesc(), ResultStatusEnum.SUCCESS.getStatus(),
                            SocketSendEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
                }
            }
            return;
        }
        List<WebSocketServer> slaveSockets = cooperateMap.remove(this.sshKey);
        if (slaveSockets != null) {
            for (WebSocketServer slaveSocket : slaveSockets) {
                IOUtils.closeQuietly(slaveSocket.getSessionSocket());
            }
        }
        // 释放ssh资源
        if (this.shellOutThread != null && !shellOutThread.isInterrupted()) {
            this.shellOutThread.interrupt();
        }
        IOUtils.closeQuietly(this.shellOutputStream, this.shellInputStream, this.shell,
                this.sftpClient, this.sshClient, this.sessionSocket);
        webSocketServerMap.remove(this.sshKey);
        closeTransClient(this.sshKey);
        // 删除临时文件
        new Thread(() -> {
            // 延时5s执行
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                LogUtil.logException(this.getClass(), e);
            }
            File fileBaseFolder = new File(FileUtil.folderBasePath);
            File[] files = fileBaseFolder.listFiles();
            if (files == null) return;
            for (File file : files) {
                // 判断是否是本次ssh对应的临时文件夹
                if (file.isDirectory() && file.getName().startsWith(this.sshKey)) {
                    FileTransInfo fileTransInfo = getTransportingFile(this.sshKey, file.getName().substring(this.sshKey.length() + 1));
                    // 忽略上传中/下载中的文件
                    if (fileTransInfo != null && fileTransInfo.getIndex() != null
                            && (fileTransInfo.getIndex().equals(1) || fileTransInfo.getIndex().equals(2)))
                        continue;
                    FileUtil.fileDelete(file);
                }
            }
        }).start();
    }
    // 关闭传输资源
    public static void closeTransClient(String sshKey) {
        // 当前没有仍在传输的文件
        if (fileTransportingMap.get(sshKey) == null || fileTransportingMap.get(sshKey).isEmpty()) {
            fileTransportingMap.remove(sshKey);
            SFTPClient sftp = sftpClientMap.remove(sshKey);
            SSHClient ssh = sshClientMap.remove(sshKey);
            IOUtils.closeQuietly(sftp, ssh);
        }
    }

    // 从Client接收消息
    @OnMessage
    public void onMessage(String message) throws Exception {
        if (this.shell == null || this.shellOutputStream == null) return;

        message = AESUtil.decrypt(message, this.secretKey);
        MessageInfo messageInfo = JSONObject.parseObject(message, MessageInfo.class);

        // 修改虚拟终端窗口大小
        if (SocketMessageEnum.SIZE_CHANGE.getState().equals(messageInfo.getType())) {
            this.shell.changeWindowDimensions(messageInfo.getCols(),messageInfo.getRows(),0,0);
        }

        // 文本命令
        if (SocketMessageEnum.USER_TEXT.getState().equals(messageInfo.getType())) {
            this.shellOutputStream.write(messageInfo.getContent().getBytes(this.serverCharset));
            this.shellOutputStream.flush();
        }
        // WebSocket心跳续约
        if (SocketMessageEnum.HEART_BEAT.getState().equals(messageInfo.getType())) {
            // SSH心跳续约
            SSHUtil.doHeartBeat(this.sshClient);
            SSHUtil.doHeartBeat(SSHUtil.getTransSSHClient(this.sshKey));
            // 更新协作者数量
            List<WebSocketServer> slaveSockets = cooperateMap.get(this.sshKey);
            if (slaveSockets != null) {
                this.sendMessage(SocketSendEnum.COOPERATE_NUMBER_UPDATE.getDesc(), ResultStatusEnum.SUCCESS.getStatus(),
                        SocketSendEnum.COOPERATE_NUMBER_UPDATE.getState(), Integer.toString(slaveSockets.size()));
            }
        }

    }

    @OnError
    public void onError(Session sessionSocket, Throwable e) {
        LogUtil.logException(this.getClass(), e);
    }

    // 向Client发送信息
    public void sendMessage(String message, String type, Integer code, String data) {

        synchronized (this.sessionSocket) {
            try {
                Result result;
                if (ResultStatusEnum.SUCCESS.getStatus().equals(type)) {
                    result = Result.success(code, message);
                } else if (ResultStatusEnum.WARNING.getStatus().equals(type)) {
                    result = Result.warning(code, message);
                } else {
                    result = Result.error(code, message);
                }
                // 重写数据加密逻辑
                if (data != null) {
                    String encryptedData = AESUtil.encrypt(data, this.secretKey);
                    result.setData(encryptedData);
                }
                this.sessionSocket.getBasicRemote().sendText(JSON.toJSONString(result));
            } catch(Exception e) {
                LogUtil.logException(this.getClass(), e);
            }
        }
    }

}