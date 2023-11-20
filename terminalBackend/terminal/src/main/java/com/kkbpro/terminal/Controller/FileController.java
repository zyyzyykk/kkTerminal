package com.kkbpro.terminal.Controller;

import com.kkbpro.terminal.Consumer.WebSocketServer;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件上传下载接口类
 */
@RestController
public class FileController {

    /**
     * 下载文件
     */
    @GetMapping("/api/daaaaaFile")
    public ResponseEntity<byte[]> downloadFile(HttpServletResponse response, String sshKey, String fileName) throws IOException {

        // 读取文件内容
        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        String remoteFilePath = "/root/" + fileName;
        readRemoteFile(sshClient, remoteFilePath,response);

        // 构建 HTTP 响应，触发文件下载
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
    }
    private void readRemoteFile(SSHClient ssh, String remoteFilePath, HttpServletResponse response) throws IOException {
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            try (RemoteFile file = sftp.open(remoteFilePath)) {
                try (InputStream is = file.new RemoteFileInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        // 逐块传输至前端
                        response.getOutputStream().write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/api/ls")
    public void ls(String sshKey) throws IOException {

        // 读取文件内容
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            List<RemoteResourceInfo> ls = sftp.ls(".");
            for (RemoteResourceInfo dd : ls) {
                System.out.println(dd.getName());
            }
        }

    }






}
