package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.constants.enums.FileStateEnum;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.exception.MyException;
import com.kkbpro.terminal.result.Result;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;
import java.io.InputStream;

public class SSHUtil {

    // 服务器编码集
    public static final ThreadLocal<String> charset = new ThreadLocal<>();

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
                    byte[] buffer = new byte[8192];
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
     * 获取单例sftpClient
     */
    public static SFTPClient getSftpClient(String sshKey) throws IOException {
        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        if (sshClient == null) {
            throw new MyException(Result.error(FileStateEnum.SSH_NOT_EXIST.getState(), "连接已断开"));
        }
        // 单例-懒汉
        if (WebSocketServer.sftpClientMap.get(sshKey) == null) {
            synchronized (sshClient) {
                if (WebSocketServer.sftpClientMap.get(sshKey) == null) {
                    SFTPClient sftpClient = sshClient.newSFTPClient();
                    WebSocketServer.sftpClientMap.put(sshKey, sftpClient);
                }
            }
        }
        return WebSocketServer.sftpClientMap.get(sshKey);
    }

    /**
     * 关闭sftp资源
     */
    public static void sftpClose(String sshKey) {
        if (WebSocketServer.webSocketServerMap.get(sshKey) == null || WebSocketServer.webSocketServerMap.get(sshKey).getSessionSocket() == null) {
            WebSocketServer.sftpClose(sshKey);
        }
    }

}
