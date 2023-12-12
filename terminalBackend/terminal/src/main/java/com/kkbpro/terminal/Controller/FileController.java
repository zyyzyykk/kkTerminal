package com.kkbpro.terminal.Controller;

import com.kkbpro.terminal.Constants.Enum.FileUploadStateEnum;
import com.kkbpro.terminal.Consumer.WebSocketServer;
import com.kkbpro.terminal.Pojo.Dto.FileUploadInfo;
import com.kkbpro.terminal.Pojo.FileInfo;
import com.kkbpro.terminal.Result.Result;
import com.kkbpro.terminal.Utils.FileUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传下载接口类
 */
@RestController
@RequestMapping("/api")
public class FileController {

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    public void downloadFile(HttpServletResponse response, String sshKey, String path, @PathVariable String fileName) throws IOException {

        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        String remoteFilePath = path + fileName;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        readRemoteFile(sshClient, remoteFilePath,response);
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
        // 读取文件内容
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        Map<String,Object> map = new HashMap<>();
        List<FileInfo> fileInfoList = new ArrayList<>();
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            List<RemoteResourceInfo> files = sftp.ls(path);
            for(RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(file.getName());
                fileInfo.setIsDirectory(file.isDirectory());
                fileInfo.setAttributes(file.getAttributes());
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
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        return Result.setSuccess(200, "首次路径", map);
    }

    @PostMapping("/upload")
    public Result uploadFile(FileUploadInfo fileUploadInfo) throws IOException {

        String sshKey = fileUploadInfo.getSshKey();
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

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);

        try (SFTPClient sftpClient = ssh.newSFTPClient()) {
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
                return Result.setError(FileUploadStateEnum.UPLOAD_ERROR.getState(), "文件片上传失败", map);
            }

            // 上传完毕
            if(chunk.equals(chunks))
            {
                // 将文件片合并
                if(!chunks.equals(1))
                    FileUtil.fileChunkMerge(folderPath,fileName,chunks,totalSize);
                // 上传到服务器
                sftpClient.put(folderPath + "/" + fileName, path + fileName);
                // 删除临时文件
                Thread deleteTmpFileThread = new Thread(() -> {
                    FileUtil.tmpFloderDelete(temporaryFolder);
                });
                deleteTmpFileThread.start();
                return Result.setSuccess(FileUploadStateEnum.FILE_UPLOAD_SUCCESS.getState(), "文件上传完成",map);
            }
            else {
                return Result.setSuccess(FileUploadStateEnum.CHUNK_UPLOAD_SUCCESS.getState(), "文件片上传成功", map);
            }
        }
    }

}
