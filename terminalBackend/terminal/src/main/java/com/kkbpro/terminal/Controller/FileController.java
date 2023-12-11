package com.kkbpro.terminal.Controller;

import com.kkbpro.terminal.Consumer.WebSocketServer;
import com.kkbpro.terminal.Pojo.FileInfo;
import com.kkbpro.terminal.Result.Result;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传下载接口类
 */
@RestController
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
        Map<String,Object> map = new HashMap<>();
        map.put("path",path);
        return Result.setSuccess(200,"首次路径",map);
    }



//    @GetMapping("/pwdddd")
//    public Result dddpwd(String sshKey) throws IOException {
//
//        String path = "/";
//        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
//        try (SFTPClient sftp = ssh.newSFTPClient()) {
//            path = sftp.canonicalize(".");
//            sftp.rename();
//
//        }
//        Map<String,Object> map = new HashMap<>();
//        map.put("path",path);
//        return Result.setSuccess(200,"首次路径",map);
//    }


    @PostMapping("/upload")
    public void uploadFile(MultipartFile file, String sshKey, String SFTP_REMOTE_DIR) throws IOException {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);

        try (SFTPClient sftpClient = ssh.newSFTPClient()) {
            String fileName = file.getOriginalFilename();
            String uniqueIdentifier = generateUniqueIdentifier();

            // Create a temporary directory for storing the file chunks
            File tempDir = new File(System.getProperty("java.io.tmpdir"), uniqueIdentifier);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Split the file into chunks and upload each chunk
            byte[] buffer = new byte[1024 * 1024]; // 1MB buffer size
            int chunkIndex = 0;
            try (FileOutputStream outputStream = new FileOutputStream(new File(tempDir, String.valueOf(chunkIndex)))) {
                int bytesRead;
                while ((bytesRead = file.getInputStream().read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    chunkIndex++;
                }
            }

            // Upload each chunk to the remote directory
            for (int i = 0; i < chunkIndex; i++) {
                File chunkFile = new File(tempDir, String.valueOf(i));
                // 将切片文件上传到远程目录
                sftpClient.put(chunkFile.getAbsolutePath(), SFTP_REMOTE_DIR + uniqueIdentifier + "/" + chunkFile.getName());
            }

            // Merge the chunks into a single file on the remote server
            // 将切片合并成一个完整的文件在远程服务器上
            sftpClient.rename(SFTP_REMOTE_DIR + uniqueIdentifier, SFTP_REMOTE_DIR + fileName);


            // Delete the temporary directory
            deleteDirectory(tempDir);
        }
    }

    private String generateUniqueIdentifier() {
        // Generate a unique identifier for the file chunks
        // You can use a UUID or any other method to generate a unique identifier
        return UUID.randomUUID().toString();
    }

    private void deleteDirectory(File directory) {
        // Recursively delete a directory
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }


















//    private static void uploadFile(SFTPClient sftpClient, String localFilePath, String remoteFilePath) throws IOException {
//        File localFile = new File(localFilePath);
//        long fileSize = localFile.length();
//        long uploadedSize = 0;
//
//        try (var inputStream = Files.newInputStream(Path.of(localFilePath), StandardOpenOption.READ)) {
//            while (uploadedSize < fileSize) {
//                int chunkSize = (int) Math.min(CHUNK_SIZE, fileSize - uploadedSize);
//                byte[] chunk = new byte[chunkSize];
//                inputStream.read(chunk);
//
//                sftpClient.put(new String(chunk), remoteFilePath, null, SFTPClient.OVERWRITE);
//
//                uploadedSize += chunkSize;
//                System.out.println("Uploaded: " + uploadedSize + " / " + fileSize);
//            }
//        } catch (SFTPException e) {
//            if (e.getStatusCode() != StatusCode.EOF) {
//                throw e;
//            }
//        }
//    }


}
