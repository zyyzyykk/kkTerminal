package com.kkbpro.terminal.Controller;

import com.kkbpro.terminal.Config.AppConfig;
import com.kkbpro.terminal.Constants.Enum.FileBlockStateEnum;
import com.kkbpro.terminal.Consumer.WebSocketServer;
import com.kkbpro.terminal.Pojo.Dto.FileUploadInfo;
import com.kkbpro.terminal.Pojo.EnvInfo;
import com.kkbpro.terminal.Pojo.FileInfo;
import com.kkbpro.terminal.Result.Result;
import com.kkbpro.terminal.Utils.FileUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * 文件上传下载接口类
 */
@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private AppConfig appConfig;

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    public void downloadFile(HttpServletResponse response, String sshKey, String path, @PathVariable String fileName) throws IOException {

        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        String remoteFilePath = path + fileName;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));
        readRemoteFile(sshClient, remoteFilePath, response);
    }
    private void readRemoteFile(SSHClient ssh, String remoteFilePath, HttpServletResponse response) throws IOException {
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            try (RemoteFile file = sftp.open(remoteFilePath)) {
                try (InputStream is = file.new RemoteFileInputStream()) {
                    byte[] buffer = new byte[8096];
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
        Map<String,Object> map = new HashMap<>();
        List<FileInfo> fileInfoList = new ArrayList<>();
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"文件列表获取失败",map);
        }
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            List<RemoteResourceInfo> files = sftp.ls(path);
            for(RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(file.getName());

                // 是否为文件夹
                if(file.isDirectory()) fileInfo.setIsDirectory(true);
                else if(file.isRegularFile()) fileInfo.setIsDirectory(false);
                else
                {
                    try {
                        fileInfo.setIsDirectory(FileMode.Type.DIRECTORY.equals(sftp.stat(path + "/" + file.getName()).getType()));
                    } catch (net.schmizz.sshj.sftp.SFTPException e) {
                        e.printStackTrace();
                        fileInfo.setIsDirectory(false);
                    }
                }

                fileInfo.setAttributes(file.getAttributes());
                fileInfoList.add(fileInfo);
            }
        } catch (net.schmizz.sshj.sftp.SFTPException e) {
            e.printStackTrace();
            return Result.setError(500,"目录不存在",map);
        }
        map.put("files",fileInfoList);

        return Result.setSuccess(200,"文件列表",map);
    }

    /**
     * 获取当前路径 pwd (首次有效)
     */
    @GetMapping("/pwd")
    public Result pwd(String sshKey) throws IOException {

        Map<String, Object> map = new HashMap<>();
        String path = "/";
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            map.put("path", path);
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件路径获取失败",map);
        }
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            path = sftp.canonicalize(".");
        }
        map.put("path", path);
        return Result.setSuccess(200, "首次路径", map);
    }

    @PostMapping("/upload")
    public Result uploadFile(FileUploadInfo fileUploadInfo) {

        String sshKey = fileUploadInfo.getSshKey();
        // 判断连接状态
        if(WebSocketServer.sshClientMap.get(sshKey) == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件上传失败");
        }

        MultipartFile file = fileUploadInfo.getFile();
        String fileName = fileUploadInfo.getFileName();
        String path = fileUploadInfo.getPath();
        String id = fileUploadInfo.getId();
        Integer chunks = fileUploadInfo.getChunks();
        Integer chunk = fileUploadInfo.getChunk();
        Long totalSize = fileUploadInfo.getTotalSize();

        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;

        Map<String,Object> map = new HashMap<>();
        map.put("chunk",chunk);
        map.put("chunks",chunks);
        map.put("id",id);
        map.put("fileName",fileName);
        map.put("totalSize",totalSize);

        File temporaryFolder = new File(folderPath);
        File temporaryFile = null;
        if(!chunks.equals(1)) {
            temporaryFile = new File(folderPath + "/" + fileName + "-" + chunk);
        }
        else temporaryFile = new File(folderPath + "/" + fileName);
        // 如果文件夹不存在则创建
        if (!temporaryFolder.exists()) {
            temporaryFolder.mkdirs();
        }
        // 如果文件存在则删除
        if (temporaryFile.exists()) {
            temporaryFile.delete();
        }
        try {
            file.transferTo(temporaryFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.setError(FileBlockStateEnum.UPLOAD_ERROR.getState(), "文件片上传失败", map);
        }

        // 上传完毕
        if(chunk.equals(chunks))
        {
            Thread FileThread = new Thread(() -> {
                WebSocketServer.fileUploadingMap.put(folderPath,folderPath);
                // 将文件片合并
                if(!chunks.equals(1))
                    FileUtil.fileChunkMerge(folderPath,fileName,chunks,totalSize);
                // 与服务器建立连接
                EnvInfo envInfo = WebSocketServer.envInfoMap.get(sshKey);
                String host = envInfo.getServer_ip();
                int port = envInfo.getServer_port();
                String user_name = envInfo.getServer_user();
                String password = envInfo.getServer_password();

                try(SSHClient sshFileClient = new SSHClient()) {
                    sshFileClient.setConnectTimeout(appConfig.getSshMaxTimeout());
                    sshFileClient.addHostKeyVerifier(new PromiscuousVerifier());    // 不验证主机密钥
                    sshFileClient.connect(host,port);
                    sshFileClient.authPassword(user_name, password);                // 使用用户名和密码进行身份验证
                    // 上传到服务器
                    try (SFTPClient sftpFileClient = sshFileClient.newSFTPClient()) {
                        sftpFileClient.put(folderPath + "/" + fileName, path + fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 删除临时文件
                    FileUtil.tmpFloderDelete(temporaryFolder);
                    WebSocketServer.fileUploadingMap.remove(folderPath);
                }
            });
            FileThread.start();
            return Result.setSuccess(FileBlockStateEnum.FILE_UPLOADING.getState(), "文件后台上传中",map);
        }
        else {
            return Result.setSuccess(FileBlockStateEnum.CHUNK_UPLOAD_SUCCESS.getState(), "文件片上传成功", map);
        }
    }

}
