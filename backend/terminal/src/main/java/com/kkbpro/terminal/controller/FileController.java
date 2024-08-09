package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.constants.enums.FileBlockStateEnum;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.exception.MyException;
import com.kkbpro.terminal.pojo.dto.FileUploadInfo;
import com.kkbpro.terminal.pojo.vo.FileInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.FileUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;

/**
 * 文件管理接口类
 */
@RestController
@RequestMapping("/api")
public class FileController {

    /**
     * 下载远程文件
     */
    @GetMapping("/download/remote/file/{fileName}")
    public void downloadRemoteFile(HttpServletResponse response, String sshKey, String path, @PathVariable String fileName) throws IOException {

        SFTPClient sftpClient = getSftpClient(sshKey);
        String remoteFilePath = path + fileName;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));
        readRemoteFile(sftpClient, remoteFilePath, response);
    }
    private void readRemoteFile(SFTPClient sftp, String remoteFilePath, HttpServletResponse response) throws IOException {
        try (RemoteFile file = sftp.open(remoteFilePath)) {
            try (InputStream is = file.new RemoteFileInputStream()) {
                byte[] buffer = new byte[8096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    // 逐块传输至前端
                    response.getOutputStream().write(buffer, 0, len);
                }
            }
        }
    }


    /**
     * 下载远程文件夹
     */
    @GetMapping("/download/remote/folder/{folderName}")
    public void downloadRemoteFolder(HttpServletResponse response, String sshKey, String path, @PathVariable String folderName) throws IOException {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) return;
        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(folderName + ".tar.gz","UTF-8"));
        try(Session session = ssh.startSession()) {
            // 进入目录并打包
            String command = "cd " + path + " && tar -czvf - " + folderName + " | less";
            Session.Command cmd = session.exec(command);
            try (InputStream tarStream = cmd.getInputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = tarStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                }
            }
            // 等待命令执行完毕
            cmd.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 下载本地文件
     * --方法未使用--
     */
    @GetMapping("/download/local/{fileName}")
    public void downloadLocalFile(HttpServletResponse response, String sshKey, String id, @PathVariable String fileName) throws IOException {

        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;
        File file = new File(folderPath + "/" + fileName);
        // 文件不存在
        if(!file.exists()) return;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));

        try(InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                // 逐块传输至前端
                response.getOutputStream().write(buffer, 0, len);
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
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            List<RemoteResourceInfo> files = sftp.ls(path);
            int index = 0;
            for(RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setIndex(index++);
                fileInfo.setId(UUID.randomUUID().toString());
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
            String errorMessage = e.getMessage();
            if("Permission denied".equals(errorMessage))
                return Result.setError(500,"无权访问此目录",map);
            else return Result.setError(500,"目录不存在",map);
        }
        map.put("files",fileInfoList);

        return Result.setSuccess(200,"文件列表",map);
    }


    /**
     * 统计所有文件/文件夹数目
     * --方法未使用--
     */
    @PostMapping("/find")
    public Result find(String sshKey, String path) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹删除失败",null);
        }
        String num;
        try(Session session = ssh.startSession()) {
            String command = "find " + path + " | wc -l";
            Session.Command cmd = session.exec(command);
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                num = reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.setError(500, "统计数目失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "统计数目失败", null);
        }
        return Result.setSuccess(200, "统计数目成功", num);
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
        SFTPClient sftp = getSftpClient(sshKey);
        path = sftp.canonicalize(".");
        map.put("path", path);
        return Result.setSuccess(200, "首次路径", map);
    }

    /**
     * 删除文件/文件夹
     * --方法未使用--
     */
    @PostMapping("/rm")
    public Result rm(String sshKey, Boolean isDirectory, String path) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹删除失败",null);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            if(isDirectory) rmFloder(sftp,path);
            else sftp.rm(path);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "删除失败", null);
        }
        return Result.setSuccess(200, "删除成功", null);
    }

    private void rmFloder(SFTPClient sftp, String path) throws IOException {
        List<RemoteResourceInfo> files = sftp.ls(path);
        for(RemoteResourceInfo file : files) {
            String tmp = path + "/" + file.getName();
            if(file.isDirectory()) rmFloder(sftp,tmp);
            else sftp.rm(tmp);
        }
        sftp.rmdir(path);
    }


    /**
     * rm -rf 快速批量删除
     */
    @PostMapping("/rm-rf")
    public Result rmRf(String sshKey, String path, String items) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹删除失败",null);
        }
        try(Session session = ssh.startSession()) {
            String command = "cd " + path + " && rm -rf " + items;
            Session.Command cmd = session.exec(command);
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.setError(500, "删除失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "删除失败", null);
        }
        return Result.setSuccess(200, "删除成功", null);
    }

    /**
     * cp 批量复制
     */
    @PostMapping("/cp")
    public Result cp(String sshKey, String src, String dst, String items) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹删除失败",null);
        }
        try(Session session = ssh.startSession()) {
            String command = "cd " + src + " && cp -n " + items + " " + dst;
            Session.Command cmd = session.exec(command);
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.setError(500, "复制失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "复制失败", null);
        }
        return Result.setSuccess(200, "复制成功", null);
    }


    /**
     * mv 批量移动
     */
    @PostMapping("/mv")
    public Result mv(String sshKey, String src, String dst, String items) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹删除失败",null);
        }
        try(Session session = ssh.startSession()) {
            String command = "cd " + src + " && mv -n " + items + " " + dst;
            Session.Command cmd = session.exec(command);
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.setError(500, "移动失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "移动失败", null);
        }
        return Result.setSuccess(200, "移动成功", null);
    }


    /**
     * 新建文件
     */
    @PostMapping("/touch")
    public Result touch(String sshKey, String path) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件夹新建失败",null);
        }
        try(Session session = ssh.startSession()) {
            String command = "touch " + path;
            Session.Command cmd = session.exec(command);
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.setError(500, "文件新建失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500, "文件新建失败", null);
        }
        return Result.setSuccess(200, "文件新建成功", null);
    }


    /**
     * 新建文件夹
     */
    @PostMapping("/mkdir")
    public Result mkdir(String sshKey, String path) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件夹新建失败",null);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            sftp.mkdir(path);
        } catch (Exception e) {
            return Result.setError(500, "文件夹新建失败", null);
        }
        return Result.setSuccess(200, "文件夹新建成功", null);
    }

    /**
     * 文件/文件夹重命名
     */
    @PostMapping("/rename")
    public Result rename(String sshKey, String oldPath, String newPath) {

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件/文件夹重命名失败",null);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            sftp.rename(oldPath,newPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.setError(500,"重命名失败");
        }
        return Result.setSuccess(200, "重命名成功", null);
    }

    /**
     * 分片上传文件
     */
    @PostMapping("/upload")
    public Result uploadFile(FileUploadInfo fileUploadInfo) {

        String sshKey = fileUploadInfo.getSshKey();
        // 判断连接状态
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
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
            WebSocketServer.fileUploadingMap.put(sshKey + "-" + id, "kkterminal");
            Thread FileThread = new Thread(() -> {
                // 将文件片合并
                if(!chunks.equals(1))
                    FileUtil.fileChunkMerge(folderPath,fileName,chunks,totalSize);
                try {
                    // 上传到服务器
                    SFTPClient sftpFileClient = getSftpClient(sshKey);
                    sftpFileClient.put(folderPath + "/" + fileName, path + fileName);
                } catch (Exception e) {
                    System.out.println("文件上传失败");
                    e.printStackTrace();
                } finally {
                    // 删除临时文件
                    FileUtil.tmpFloderDelete(temporaryFolder);
                    WebSocketServer.fileUploadingMap.remove(sshKey + "-" + id);
                }
            });
            FileThread.start();
            return Result.setSuccess(FileBlockStateEnum.FILE_UPLOADING.getState(), "文件后台上传中",map);
        }
        else {
            return Result.setSuccess(FileBlockStateEnum.CHUNK_UPLOAD_SUCCESS.getState(), "文件片上传成功", map);
        }
    }


    private SFTPClient getSftpClient(String sshKey) throws IOException {
        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        if(sshClient == null) {
            throw new MyException(Result.setError(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开", null));
        }
        // 单例-懒汉
        if(WebSocketServer.sftpClientMap.get(sshKey) == null) {
            synchronized (sshClient) {
                if(WebSocketServer.sftpClientMap.get(sshKey) == null) {
                    SFTPClient sftpClient = sshClient.newSFTPClient();
                    WebSocketServer.sftpClientMap.put(sshKey, sftpClient);
                }
            }
        }
        return WebSocketServer.sftpClientMap.get(sshKey);
    }

}