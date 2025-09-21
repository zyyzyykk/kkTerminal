package com.kkbpro.terminal.utils;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;

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
            if(output != null) {
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

}
