package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.FileStateEnum;
import com.kkbpro.terminal.enums.FileUntarEnum;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.enums.ResultCodeEnum;
import com.kkbpro.terminal.pojo.dto.FileUploadInfo;
import com.kkbpro.terminal.pojo.vo.FileInfo;
import com.kkbpro.terminal.pojo.vo.FileTransInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.*;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 文件管理接口类
 */
@RestController
@RequestMapping(Constant.API_PREFIX + "/file")
public class FileController {

    /**
     * 下载远程文件
     */
    @Log
    @GetMapping("/download/remote/file")
    public void downloadRemoteFile(HttpServletResponse response, String sshKey, String path, String fileName, String trigger) throws IOException {
        SFTPClient sftpClient = SSHUtil.getTransSFTPClient(sshKey);
        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

        String id = UUID.randomUUID().toString();
        String remoteFilePath = path + fileName;
        FileTransInfo fileTransInfo = new FileTransInfo(id, path, fileName, sftpClient.size(remoteFilePath), 2, 0);
        // 浏览器下载（非文件编辑器中打开）
        if ("browser".equals(trigger)) WebSocketServer.putTransportingFile(sshKey, id, fileTransInfo);
        try (RemoteFile file = sftpClient.open(remoteFilePath);
             InputStream is = file.new RemoteFileInputStream())
        {
            byte[] buffer = new byte[Constant.BUFFER_SIZE];
            int len;
            while ((len = is.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, len);   // 流式传输
            }
        } catch (Exception e) {
            fileTransInfo.setStatus(-2);
            LogUtil.logException(this.getClass(), e);
        } finally {
            WebSocketServer.removeTransportingFile(sshKey, id);
            // 释放资源
            SSHUtil.closeTransClient(sshKey);
        }
    }


    /**
     * 下载远程文件夹
     */
    @Log
    @GetMapping("/download/remote/folder")
    public void downloadRemoteFolder(HttpServletResponse response, String sshKey, String path, String folderName) throws IOException {
        SSHClient ssh = SSHUtil.getTransSSHClient(sshKey);
        if (ssh == null) return;
        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(folderName + ".tar.gz", StandardCharsets.UTF_8.name()));

        String id = UUID.randomUUID().toString();
        // 进入目录并打包
        String command = "cd " + path + " && tar -czvf - " + folderName + " | less";
        FileTransInfo fileTransInfo = new FileTransInfo(id, path, folderName, -1L, 2, 0);
        WebSocketServer.putTransportingFile(sshKey, id, fileTransInfo);
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command);
            InputStream is = cmd.getInputStream())
        {
            byte[] buffer = new byte[Constant.BUFFER_SIZE];
            int len;
            try {
                while ((len = is.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);   // 流式传输
                }
            } catch (Exception e) {
                cmd.close();
                throw e;
            }
            // 等待命令执行完毕
            cmd.join();
        } catch (Exception e) {
            fileTransInfo.setStatus(-2);
            LogUtil.logException(this.getClass(), e);
        } finally {
            WebSocketServer.removeTransportingFile(sshKey, id);
            // 释放资源
            SSHUtil.closeTransClient(sshKey);
        }
    }


    /**
     * 下载本地文件
     * --方法未使用--
     */
    @Log
    @GetMapping("/download/local")
    public void downloadLocalFile(HttpServletResponse response, String sshKey, String id, String fileName) throws IOException {
        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;
        File file = new File(folderPath + "/" + id);
        // 文件不存在
        if (!file.exists()) return;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

        try(InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[Constant.BUFFER_SIZE];
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
    @Log
    @GetMapping("/ls")
    public Result ls(String sshKey, String path) throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();
        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接已断开");
        }
        try {
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            List<RemoteResourceInfo> files = sftp.ls(path);
            for (RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setId(UUID.randomUUID().toString());
                fileInfo.setName(file.getName());
                // 是否为引用文件
                fileInfo.setIsSymlink(file.getAttributes().getType() == FileMode.Type.SYMLINK);
                // 是否为文件夹
                if (file.isDirectory()) fileInfo.setIsDirectory(true);
                else if (file.isRegularFile()) fileInfo.setIsDirectory(false);
                else {
                    try {
                        fileInfo.setIsDirectory(FileMode.Type.DIRECTORY.equals(sftp.stat(path + "/" + file.getName()).getType()));
                    } catch (SFTPException e) {
                        LogUtil.logException(this.getClass(), e);
                        fileInfo.setIsDirectory(false);
                    }
                }
                // 文件属性
                fileInfo.setAttributes(file.getAttributes());
                fileInfoList.add(fileInfo);
            }
        } catch (SFTPException e) {
            LogUtil.logException(this.getClass(), e);
            Response.StatusCode statusCode  = e.getStatusCode();
            if (Response.StatusCode.NO_SUCH_FILE.equals(statusCode)) {
                return Result.error("目录不存在");
            }
            else if (Response.StatusCode.PERMISSION_DENIED.equals(statusCode)) {
                return Result.error("目录拒绝访问");
            }
            else {
                return Result.error("文件列表获取失败");
            }
        }

        return Result.success(200,"文件列表", fileInfoList);
    }


    /**
     * 统计文件夹中包含的文件/文件夹数目（文件数@文件夹数）
     */
    @Log
    @GetMapping("/find")
    public Result find(String sshKey, String path, String item) {
        String errorMsg = "统计数目失败";
        String successMsg = "统计数目成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder num = new StringBuilder();
        String[] nums;
        String command = "cd " + path + " && echo -n \"$(find " + item + " -type f | wc -l)@$(find " + item + " -type d | wc -l)\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, num);
            if (exitStatus != 0) return Result.error(errorMsg);
            nums = num.toString().split("@");
            for (String s : nums) Integer.parseInt(s);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, nums);
    }


    /**
     * 获取文件大小（字节）
     */
    @Log
    @GetMapping("/du")
    public Result du(String sshKey, String path, String item) {
        String errorMsg = "获取大小失败";
        String successMsg = "获取大小成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder size = new StringBuilder();
        String command = "cd " + path + " && echo -n \"$(du -sb " + item + " | head -n 1 | cut -f1)\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, size);
            if (exitStatus != 0) return Result.error(errorMsg);
            Integer.parseInt(size.toString());
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, size.toString());
    }

    /**
     * 获取家目录 (首次有效)
     */
    @Log
    @GetMapping("/home")
    public Result home(String sshKey) throws IOException {

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接已断开");
        }
        SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
        String directory = sftp.canonicalize(".");

        return Result.success(200, "家目录", directory);
    }

    /**
     * 删除文件/文件夹
     * --方法已弃用--
     */
    @Log
    @PostMapping("/rm")
    public Result rm(String sshKey, Boolean isDirectory, String path) {
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            if (isDirectory) rmFolder(sftp, path);
            else sftp.rm(path);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }
    private void rmFolder(SFTPClient sftp, String path) throws IOException {
        List<RemoteResourceInfo> files = sftp.ls(path);
        for (RemoteResourceInfo file : files) {
            String tmp = path + "/" + file.getName();
            if (file.isDirectory()) rmFolder(sftp,tmp);
            else sftp.rm(tmp);
        }
        sftp.rmdir(path);
    }


    /**
     * rm -rf 快速批量删除
     */
    @Log
    @PostMapping("/rm-rf")
    public Result rmRf(String sshKey, String path, String items) {
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && rm -rf " + items;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }

    /**
     * cp 批量复制
     */
    @Log
    @PostMapping("/cp")
    public Result cp(String sshKey, String src, String dst, String items) {
        String errorMsg = "复制失败";
        String successMsg = "复制成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = "cd " + src + " && cp -rn " + items + " " + dst;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }


    /**
     * mv 批量移动
     */
    @Log
    @PostMapping("/mv")
    public Result mv(String sshKey, String src, String dst, String items) {
        String errorMsg = "移动失败";
        String successMsg = "移动成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = "cd " + src + " && mv -n " + items + " " + dst;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }


    /**
     * 新建文件
     */
    @Log
    @PostMapping("/touch")
    public Result touch(String sshKey, String path, String item) {
        String errorMsg = "文件新建失败";
        String successMsg = "文件新建成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && test ! -e " + item + " && touch " + item;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }


    /**
     * 新建文件夹
     */
    @Log
    @PostMapping("/mkdir")
    public Result mkdir(String sshKey, String path, String item) {
        String errorMsg = "文件夹新建失败";
        String successMsg = "文件夹新建成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            String fullPath = path + item;
            if (sftp.statExistence(fullPath) != null)
                return Result.error(errorMsg);
            sftp.mkdirs(fullPath);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }

    /**
     * 文件/文件夹重命名
     */
    @Log
    @PostMapping("/rename")
    public Result rename(String sshKey, String oldPath, String newPath) {
        String errorMsg = "重命名失败";
        String successMsg = "重命名成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            sftp.rename(oldPath,newPath);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }


    /**
     * wget 文件URL上传
     */
    @Log
    @PostMapping("/wget")
    public Result wget(String sshKey, String path, String item, String url) {
        String errorMsg = "文件URL上传失败";
        String successMsg = "文件URL上传成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String ua = "--user-agent=\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36\"";
        String command = "cd " + path + " && wget " + ua + " -b -q -O " + item + " \"" + url + "\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }

    /**
     *  tar 文件压缩包解压
     */
    @Log
    @PostMapping("/untar")
    public Result untar(String sshKey, String path, String item) {
        String errorMsg = "解压失败";
        String successMsg = "解压成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        FileUntarEnum fileUntarEnum = FileUntarEnum.getByFileName(item);
        if (fileUntarEnum == null) return Result.error(errorMsg);

        String command = "cd " + path + " && " + fileUntarEnum.getCommand() + item;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }

    /**
     * chmod 文件权限修改
     */
    @Log
    @PostMapping("/chmod")
    public Result chmod(String sshKey, String path, String item, String perms, Boolean sub) {
        String errorMsg = "权限修改失败";
        String successMsg = "权限修改成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && chmod " + (sub ? "-R " : "") + perms + " " + item;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg);
    }

    /**
     * 文件片上传
     */
    @Log
    @PostMapping("/chunk/upload")
    public Result uploadFileChunk(FileUploadInfo fileUploadInfo) {

        String sshKey = fileUploadInfo.getSshKey();
        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，文件上传失败");
        }

        MultipartFile file = fileUploadInfo.getFile();
        String id = fileUploadInfo.getId();
        Integer chunk = fileUploadInfo.getChunk();

        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;
        File temporaryFolder = new File(folderPath);
        // 如果文件夹不存在则创建
        if (!temporaryFolder.exists()) {
            temporaryFolder.mkdirs();
        }
        File temporaryFile = new File(folderPath + "/" + id + "-" + chunk);
        // 如果文件存在则删除
        if (temporaryFile.exists()) {
            temporaryFile.delete();
        }
        // 写入数据
        try {
            Files.write(temporaryFile.toPath(), file.getBytes());
        } catch (Exception e) {
            temporaryFile.delete();
            LogUtil.logException(this.getClass(), e);
            return Result.error(FileStateEnum.UPLOAD_ERROR.getState(), "文件片上传失败");
        }

        return Result.success(FileStateEnum.CHUNK_UPLOAD_SUCCESS.getState(), "文件片上传成功", null);
    }

    /**
     * 文件片合并
     */
    @Log
    @PostMapping("/chunk/merge")
    public Result mergeFileChunk(FileUploadInfo fileUploadInfo) {

        String sshKey = fileUploadInfo.getSshKey();
        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，文件上传失败");
        }

        String fileName = fileUploadInfo.getFileName();
        String path = fileUploadInfo.getPath();
        String id = fileUploadInfo.getId();
        Integer chunks = fileUploadInfo.getChunks();
        Long totalSize = fileUploadInfo.getTotalSize();

        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;
        File temporaryFolder = new File(folderPath);

        FileTransInfo fileTransInfo = new FileTransInfo(id, path, fileName, totalSize, 1, 0);
        WebSocketServer.putTransportingFile(sshKey, id, fileTransInfo);
        new Thread(() -> {
            try {
                // 合并文件片
                FileUtil.fileChunkMerge(folderPath, id, chunks, totalSize);
                // 上传到服务器
                SFTPClient sftpFileClient = SSHUtil.getTransSFTPClient(sshKey);
                sftpFileClient.put(folderPath + "/" + id, path + fileName);
            } catch (Exception e) {
                fileTransInfo.setStatus(-1);
                LogUtil.logException(this.getClass(), e);
            } finally {
                WebSocketServer.removeTransportingFile(sshKey, id);
                // 删除临时文件夹
                FileUtil.fileDelete(temporaryFolder);
                // 释放资源
                SSHUtil.closeTransClient(sshKey);
            }
        }).start();

        return Result.success(FileStateEnum.FILE_UPLOADING.getState(), "文件后台上传中", null);
    }

}