package com.kkbpro.terminal.Controller;

import com.kkbpro.terminal.Consumer.WebSocketServer;
import com.kkbpro.terminal.Pojo.FileInfo;
import com.kkbpro.terminal.Result.Result;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传下载接口类
 */
@RestController
public class FileController {

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(HttpServletResponse response, String sshKey, String path, @PathVariable String fileName) throws IOException {

        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        String remoteFilePath = path + fileName;

        // 构建 HTTP 响应，触发文件下载
        readRemoteFile(sshClient, remoteFilePath,response);
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
     * 获取文件信息列表 ls
     */
    @GetMapping("/ls")
    public Result ls(String sshKey, String path) throws IOException {
        // 读取文件内容
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        Map<String,Object> map = new HashMap<>();
        List<FileInfo> fileInfoList = new ArrayList<>();
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            List<RemoteResourceInfo> files = sftp.ls(path);
            for(RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(file.getName());
                fileInfo.setType(file.isDirectory());
                fileInfoList.add(fileInfo);
            }
        }
        map.put("files",fileInfoList);

        return Result.setSuccess(200,"文件列表",map);
    }

    /**
     * 获取当前路径 pwd (首次有效)
     */
    @GetMapping("/pwd")
    public Result pwd(String sshKey) throws IOException {

        String path = "/";
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            path = sftp.canonicalize(".");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("path",path);
        return Result.setSuccess(200,"首次路径",map);
    }


}
