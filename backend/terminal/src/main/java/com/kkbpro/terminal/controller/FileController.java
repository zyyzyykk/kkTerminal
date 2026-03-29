package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.FileUploadEnum;
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

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 文件管理接口类
 */
@RestController
@RequestMapping(Constant.API_PREFIX + "/file")
public class FileController {

    public static final String transportPath = "/" + "transport" + "/";

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
     * 获取路径下所有文件信息
     */
    @Log
    @GetMapping("/ls/all")
    public Result lsAll(String sshKey, String path) throws IOException {
        String errorMsg = "获取文件列表失败";
        String successMsg = "获取文件列表成功";

        Map<String, Object> map = new HashMap<>();
        List<FileInfo> fileInfoList = new ArrayList<>();
        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接已断开");
        }
        try {
            if (!"/".equals(path) && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            // 路径为一个文件
            if (!FileMode.Type.DIRECTORY.equals(sftp.stat(path).getType())) {
                int index = path.lastIndexOf("/");
                String fileName = path.substring(index + 1);
                map.put("fileName", fileName);
                path = path.substring(0, index);
            }
            // 转化为绝对路径
            path = sftp.canonicalize(path);
            if (!"/".equals(path) && !path.endsWith("/")) {
                path += "/";
            }
            map.put("path", path);

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
            map.put("fileInfoList", fileInfoList);
        } catch (SFTPException e) {
            LogUtil.logException(this.getClass(), e);
            Response.StatusCode statusCode = e.getStatusCode();
            if (Response.StatusCode.NO_SUCH_FILE.equals(statusCode)) {
                return Result.error("目录不存在");
            }
            else if (Response.StatusCode.PERMISSION_DENIED.equals(statusCode)) {
                return Result.error("目录拒绝访问");
            }
            else {
                return Result.error(errorMsg);
            }
        }

        return Result.success(successMsg, map);
    }


    /**
     * 获取路径下所有文件夹名称 (以'/'分割)
     */
    @Log
    @GetMapping("/ls/folders")
    public Result lsFolders(String sshKey, String path) {
        String errorMsg = "获取文件夹名称失败";
        String successMsg = "获取文件夹名称成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
        }
        StringBuilder names = new StringBuilder();
        String command = "cd " + path + " && echo -n \"$(find . -mindepth 1 -maxdepth 1 -type d -exec basename {} \\; | paste -sd '/')\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, names);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg, names.toString());
    }


    /**
     * 获取文件补全名称
     */
    @Log
    @GetMapping("/compgen")
    public Result compgen(String sshKey, String path) {
        String errorMsg = "获取文件补全名称失败";
        String successMsg = "获取文件补全名称成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
        }
        StringBuilder names = new StringBuilder();
        String command = "echo -n \"$(compgen -f -- " + path + " | paste -sd '|')\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, names);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg, names.toString());
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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

        return Result.success(successMsg, nums);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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

        return Result.success(successMsg, size.toString());
    }

    /**
     * 获取家目录
     */
    @Log
    @GetMapping("/home")
    public Result home(String sshKey) throws IOException {
        String errorMsg = "获取家目录失败";
        String successMsg = "获取家目录成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接已断开");
        }
        StringBuilder home = new StringBuilder();
        String command = "echo -n $HOME";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, home);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(successMsg, home.toString());
    }


    /**
     * rm -rf 快速批量删除
     */
    @Log
    @PostMapping("/rm")
    public Result rm(String sshKey, String path, String items) {
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = SSHUtil.getSFTPClient(sshKey);
            String fullPath = path + item;
            // 文件夹已存在
            if (sftp.statExistence(fullPath) != null) {
                return Result.error(errorMsg);
            }
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
     * tar 文件压缩包解压
     */
    @Log
    @PostMapping("/untar")
    public Result untar(String sshKey, String path, String item) {
        String errorMsg = "解压失败";
        String successMsg = "解压成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，" + errorMsg);
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，文件上传失败");
        }

        MultipartFile file = fileUploadInfo.getFile();
        String id = fileUploadInfo.getId();
        Integer chunk = fileUploadInfo.getChunk();

        String transPath = FileUtil.tempBasePath + transportPath + sshKey;
        String transFileFolderPath = transPath + "/" + id;
        String chunkFilePath = transFileFolderPath + "/" + id + "-" + chunk;
        File chunkFile = FileUtil.prepareFile(chunkFilePath);
        // 写入数据
        try {
            file.transferTo(chunkFile.toPath());
        } catch (Exception e) {
            chunkFile.delete();
            LogUtil.logException(this.getClass(), e);
            return Result.error(FileUploadEnum.UPLOAD_ERROR.getCode(), "文件片上传失败");
        }

        return Result.success(FileUploadEnum.CHUNK_UPLOAD_SUCCESS.getCode(), "文件片上传成功");
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
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getCode(), "连接断开，文件上传失败");
        }

        String fileName = fileUploadInfo.getFileName();
        String path = fileUploadInfo.getPath();
        String id = fileUploadInfo.getId();
        Integer chunks = fileUploadInfo.getChunks();
        Long totalSize = fileUploadInfo.getTotalSize();

        String transPath = FileUtil.tempBasePath + transportPath + sshKey;
        File transFolder = FileUtil.prepareDirectory(transPath);
        String transFileFolderPath = transPath + "/" + id;
        File transFileFolder = FileUtil.prepareDirectory(transFileFolderPath);
        String transFilePath = transFileFolderPath + "/" + id;

        FileTransInfo fileTransInfo = new FileTransInfo(id, path, fileName, totalSize, 1, 0);
        WebSocketServer.putTransportingFile(sshKey, id, fileTransInfo);
        Thread.ofVirtual().start(() -> {
            try {
                // 合并文件片
                FileUtil.mergeFileChunks(transFileFolderPath, id, chunks, totalSize);
                // 上传到服务器
                SFTPClient sftp = SSHUtil.getTransSFTPClient(sshKey);
                sftp.put(transFilePath, path + fileName);
            } catch (Exception e) {
                fileTransInfo.setStatus(-1);
                LogUtil.logException(this.getClass(), e);
            } finally {
                WebSocketServer.removeTransportingFile(sshKey, id);
                // 释放资源
                Boolean isClosed = SSHUtil.closeTransClient(sshKey);
                // 删除传输文件夹
                FileUtil.forceDeleteFolder(transFileFolder);
                if (isClosed) FileUtil.forceDeleteFolder(transFolder);
            }
        });

        return Result.success(FileUploadEnum.FILE_UPLOADING.getCode(), "文件后台上传中");
    }

}