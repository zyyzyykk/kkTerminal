package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.pojo.dto.EnvInfo;
import com.kkbpro.terminal.pojo.dto.PrivateKey;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class SSHUtil {

    // 服务器编码集
    public static final ThreadLocal<String> charset = new ThreadLocal<>();

    /**
     * 连接主机
     */
    public static SSHClient connectHost(EnvInfo envInfo) throws Exception {
        // 连接信息
        String host = envInfo.getServer_ip();
        Integer port = envInfo.getServer_port();
        String username = envInfo.getServer_user();
        String password = envInfo.getServer_password();
        PrivateKey privateKey = envInfo.getServer_key();
        Integer authType = envInfo.getAuthType();
        File keyFile = null;

        // 建立SSH连接
        SSHClient sshClient = new SSHClient();
        try {
            sshClient.setConnectTimeout(Constant.SSH_CONNECT_TIMEOUT);
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());                // 不验证主机密钥
            sshClient.connect(host, port);
            if (authType != 1) sshClient.authPassword(username, password);          // 使用用户名和密码进行身份验证
            else {
                // 创建本地私钥文件
                String keyPath = FileUtil.folderBasePath + "/" + "keyProviders" + "/" + UUID.randomUUID();
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
                sshClient.authPublickey(username, keyProvider);
            }
        } finally {
            // 删除本地私钥文件
            if (keyFile != null) FileUtil.fileDelete(keyFile);
        }

        return sshClient;
    }

    /**
     * SSH心跳续约
     */
    public static void doHeartBeat(SSHClient sshClient) throws Exception {
        if (sshClient != null && sshClient.isConnected()) {
            sshClient.getTransport().write((new SSHPacket(Message.IGNORE)).putString(""));
        }
    }

    /**
     * 执行终端命令
     */
    public static Integer executeCommand(SSHClient ssh, String command, StringBuilder output) throws Exception {
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            if (output != null) {
                try (InputStream reader = cmd.getInputStream()) {
                    byte[] buffer = new byte[Constant.BUFFER_SIZE];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        output.append(new String(buffer, 0, len, charset.get()));
                    }
                }
            }
            // 等待命令执行完毕
            cmd.join();

            return cmd.getExitStatus();
        }
    }

    /**
     * 获取SSHClient
     */
    public static SSHClient getSSHClient(String sshKey) {
        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        return webSocketServer != null ? webSocketServer.getSshClient() : null;
    }

    /**
     * 获取SFTPClient
     */
    public static SFTPClient getSFTPClient(String sshKey) {
        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        return webSocketServer != null ? webSocketServer.getSftpClient() : null;
    }

    /**
     * 获取传输SSHClient
     */
    public static SSHClient getTransSSHClient(String sshKey) {
        return WebSocketServer.sshClientMap.get(sshKey);
    }

    /**
     * 获取传输SFTPClient
     */
    public static SFTPClient getTransSFTPClient(String sshKey) {
        return WebSocketServer.sftpClientMap.get(sshKey);
    }

    /**
     * 关闭传输资源
     */
    public static void closeTransClient(String sshKey) {
        // WebSocket连接已断开
        if (WebSocketServer.webSocketServerMap.get(sshKey) == null || WebSocketServer.webSocketServerMap.get(sshKey).getSessionSocket() == null) {
            WebSocketServer.closeTransClient(sshKey);
        }
    }

}
