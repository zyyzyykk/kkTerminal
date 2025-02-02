package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.constants.enums.FileBlockStateEnum;
import com.kkbpro.terminal.constants.enums.FileUntarEnum;
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
    @GetMapping("/download/remote/file")
    public void downloadRemoteFile(HttpServletResponse response, String sshKey, String path, String fileName) throws IOException {
        SFTPClient sftpClient = getSftpClient(sshKey);
        String remoteFilePath = path + fileName;

        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));
        readRemoteFile(sshKey, sftpClient, remoteFilePath, response);
    }
    private void readRemoteFile(String sshKey, SFTPClient sftp, String remoteFilePath, HttpServletResponse response) throws IOException {
        String id = UUID.randomUUID().toString();
        WebSocketServer.fileUploadingMap.get(sshKey).put(id, "kkterminal");
        try (RemoteFile file = sftp.open(remoteFilePath)) {
            try (InputStream is = file.new RemoteFileInputStream()) {
                byte[] buffer = new byte[8096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    // 逐块传输至前端
                    response.getOutputStream().write(buffer, 0, len);
                }
            }
        } finally {
            WebSocketServer.fileUploadingMap.get(sshKey).remove(id);
            // 释放资源
            sshClose(sshKey);
        }
    }


    /**
     * 下载远程文件夹
     */
    @GetMapping("/download/remote/folder")
    public void downloadRemoteFolder(HttpServletResponse response, String sshKey, String path, String folderName) throws IOException {
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) return;
        // 构建 HTTP 响应，触发文件下载
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(folderName + ".tar.gz","UTF-8"));

        String id = UUID.randomUUID().toString();
        // 进入目录并打包
        String command = "cd " + path + " && tar -czvf - " + folderName + " | less";
        WebSocketServer.fileUploadingMap.get(sshKey).put(id, "kkterminal");
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command);
            InputStream tarStream = cmd.getInputStream())
        {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = tarStream.read(buffer)) != -1) {
                try {
                    if(response != null) response.getOutputStream().write(buffer, 0, len);
                } catch (Exception e) {
                    response = null;
                }
            }
            // 等待命令执行完毕
            cmd.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            WebSocketServer.fileUploadingMap.get(sshKey).remove(id);
            // 释放资源
            sshClose(sshKey);
        }
    }


    /**
     * 下载本地文件
     * --方法未使用--
     */
    @GetMapping("/download/local")
    public void downloadLocalFile(HttpServletResponse response, String sshKey, String id, String fileName) throws IOException {
        String folderPath = FileUtil.folderBasePath + "/" + sshKey + "-" + id;
        File file = new File(folderPath + "/" + id);
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
        List<FileInfo> fileInfoList = new ArrayList<>();
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接已断开");
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            List<RemoteResourceInfo> files = sftp.ls(path);
            // 按照文件名排序
            files.sort(Comparator.comparing(RemoteResourceInfo::getName));
            int index = 0;
            for(RemoteResourceInfo file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setIndex(index++);
                fileInfo.setId(UUID.randomUUID().toString());
                fileInfo.setName(file.getName());
                // 是否为引用文件
                if(file.getAttributes().getType() == FileMode.Type.SYMLINK)
                    fileInfo.setIsSymlink(true);
                else fileInfo.setIsSymlink(false);
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
            Response.StatusCode statusCode  = e.getStatusCode();
            if(Response.StatusCode.NO_SUCH_FILE.equals(statusCode)) {
                return Result.error("目录不存在");
            }
            else if(Response.StatusCode.PERMISSION_DENIED.equals(statusCode)) {
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
    @GetMapping("/find")
    public Result find(String sshKey, String path, String item) {
        String errorMsg = "统计数目失败";
        String successMsg = "统计数目成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String num = "";
        String[] nums;
        String command = "cd " + path + " && echo \"$(find " + item + " -type f | wc -l)@$(find " + item + " -type d | wc -l)\"";
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                num += reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
            nums = num.split("@");
            for (String s : nums) Integer.parseInt(s);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(200, successMsg, nums);
    }


    /**
     * 获取文件大小（字节）
     */
    @GetMapping("/du")
    public Result du(String sshKey, String path, String item) {
        String errorMsg = "获取大小失败";
        String successMsg = "获取大小成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String size = "";
        String command = "cd " + path + " && du -sb " + item + " | head -n 1 | cut -f1";
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                size += reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
            Integer.parseInt(size);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(200, successMsg, size);
    }

    /**
     * 获取家路径 (首次有效)
     */
    @GetMapping("/home")
    public Result home(String sshKey) throws IOException {
        String path = "/";
        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接已断开");
        }
        SFTPClient sftp = getSftpClient(sshKey);
        path = sftp.canonicalize(".");
        return Result.success(200, "家路径", path);
    }

    /**
     * 获取当前目录（pwd）
     * --方法未使用--
     */
    @GetMapping("/pwd")
    public Result pwd(String sshKey) {
        String errorMsg = "获取当前目录失败";
        String successMsg = "获取当前目录成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String path = "";
        String command = "pwd";
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                path += reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(200, successMsg, path);
    }

    /**
     * 删除文件/文件夹
     * --方法已弃用--
     */
    @PostMapping("/rm")
    public Result rm(String sshKey, Boolean isDirectory, String path) {
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            if(isDirectory) rmFloder(sftp,path);
            else sftp.rm(path);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
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
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && rm -rf " + items;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }

    /**
     * cp 批量复制
     */
    @PostMapping("/cp")
    public Result cp(String sshKey, String src, String dst, String items) {
        String errorMsg = "复制失败";
        String successMsg = "复制成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String command = "cd " + src + " && cp -rn " + items + " " + dst;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }


    /**
     * mv 批量移动
     */
    @PostMapping("/mv")
    public Result mv(String sshKey, String src, String dst, String items) {
        String errorMsg = "移动失败";
        String successMsg = "移动成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String command = "cd " + src + " && mv -n " + items + " " + dst;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }


    /**
     * 新建文件
     */
    @PostMapping("/touch")
    public Result touch(String sshKey, String path, String item) {
        String errorMsg = "文件新建失败";
        String successMsg = "文件新建成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && test ! -e " + item + " && touch " + item;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }


    /**
     * 新建文件夹
     */
    @PostMapping("/mkdir")
    public Result mkdir(String sshKey, String path, String item) {
        String errorMsg = "文件夹新建失败";
        String successMsg = "文件夹新建成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            String fullPath = path + item;
            if(sftp.statExistence(fullPath) != null)
                return Result.error(errorMsg);
            sftp.mkdirs(fullPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }

    /**
     * 文件/文件夹重命名
     */
    @PostMapping("/rename")
    public Result rename(String sshKey, String oldPath, String newPath) {
        String errorMsg = "重命名失败";
        String successMsg = "重命名成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        try {
            SFTPClient sftp = getSftpClient(sshKey);
            sftp.rename(oldPath,newPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }


    /**
     * wget 文件URL上传
     */
    @PostMapping("/wget")
    public Result wget(String sshKey, String path, String item, String url) {
        String errorMsg = "文件URL上传失败";
        String successMsg = "文件URL上传成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String ua = "--user-agent=\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36\"";
        String command = "cd " + path + " && wget " + ua + " -b -q -O " + item + " \"" + url + "\"";
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }

    /**
     *  tar 文件压缩包解压
     */
    @PostMapping("/untar")
    public Result untar(String sshKey, String path, String item) {
        String errorMsg = "解压失败";
        String successMsg = "解压成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        FileUntarEnum fileUntarEnum = FileUntarEnum.getByFileName(item);
        if(fileUntarEnum == null) return Result.error(errorMsg);

        String command = "cd " + path + " && " + fileUntarEnum.getCmdParam() + " " + item;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
    }

    /**
     * chmod 文件权限修改
     */
    @PostMapping("/chmod")
    public Result chmod(String sshKey, String path, String item, String perms, Boolean sub) {
        String errorMsg = "权限修改失败";
        String successMsg = "权限修改成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String command = "cd " + path + " && chmod " + (sub ? "-R " : "") + perms + " " + item;
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(successMsg);
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
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，文件上传失败");
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
            temporaryFile = new File(folderPath + "/" + id + "-" + chunk);
        }
        else temporaryFile = new File(folderPath + "/" + id);
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
            return Result.error(FileBlockStateEnum.UPLOAD_ERROR.getState(), "文件片上传失败", map);
        }

        // 上传完毕
        if(chunk.equals(chunks))
        {
            WebSocketServer.fileUploadingMap.get(sshKey).put(id, "kkterminal");
            Thread FileThread = new Thread(() -> {
                try {
                    // 将文件片合并
                    if(!chunks.equals(1))
                        FileUtil.fileChunkMerge(folderPath,id,chunks,totalSize);
                    // 上传到服务器
                    SFTPClient sftpFileClient = getSftpClient(sshKey);
                    sftpFileClient.put(folderPath + "/" + id, path + fileName);
                } catch (Exception e) {
                    System.out.println("文件上传失败");
                    e.printStackTrace();
                } finally {
                    // 删除临时文件
                    FileUtil.fileDelete(temporaryFolder);
                    WebSocketServer.fileUploadingMap.get(sshKey).remove(id);
                    // 释放资源
                    sshClose(sshKey);
                }
            });
            FileThread.start();
            return Result.success(FileBlockStateEnum.FILE_UPLOADING.getState(), "文件后台上传中", map);
        }
        else {
            return Result.success(FileBlockStateEnum.CHUNK_UPLOAD_SUCCESS.getState(), "文件片上传成功", map);
        }
    }


    private SFTPClient getSftpClient(String sshKey) throws IOException {
        SSHClient sshClient = WebSocketServer.sshClientMap.get(sshKey);
        if(sshClient == null) {
            throw new MyException(Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开"));
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

    private void sshClose(String sshKey) {
        if((WebSocketServer.webSocketServerMap.get(sshKey) == null || WebSocketServer.webSocketServerMap.get(sshKey).getSessionSocket() == null)
                && (WebSocketServer.fileUploadingMap.get(sshKey) == null || WebSocketServer.fileUploadingMap.get(sshKey).isEmpty())) {
            try {
                WebSocketServer.fileUploadingMap.remove(sshKey);
                if(WebSocketServer.sftpClientMap.get(sshKey) != null)
                    WebSocketServer.sftpClientMap.get(sshKey).close();
                WebSocketServer.sftpClientMap.remove(sshKey);
                if(WebSocketServer.sshClientMap.get(sshKey) != null)
                    WebSocketServer.sshClientMap.get(sshKey).close();
                WebSocketServer.sshClientMap.remove(sshKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}