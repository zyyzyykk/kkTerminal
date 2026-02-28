package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.FileUploadEnum;
import com.kkbpro.terminal.enums.ResultCodeEnum;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.FileUtil;
import com.kkbpro.terminal.utils.LogUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 云端同步
 **/
@RestController
@RequestMapping(Constant.API_PREFIX + "/cloud")
public class CloudController {

    private static final String cloudBasePath = System.getProperty("user.dir") + "/" + "cloud";

    private static final String countType = "record-";

    private static final Integer maxCount = 100;

    private static final Integer expirationDays = 7;

    /**
     * 上传文件
     */
    @Log
    @PostMapping("/upload")
    public Result uploadCloud(String user, String type, String name, MultipartFile file) throws IOException {
        String userFolderPath = cloudBasePath + "/" + user;
        File userFolder = FileUtil.prepareDirectory(userFolderPath);
        File[] userFiles = userFolder.listFiles();
        // 限制录像文件数量
        if (userFiles != null && userFiles.length > maxCount && countType.equals(type)) {
            return Result.error(ResultCodeEnum.CLOUD_COUNT_ERROR.getCode(),"云端文件过多");
        }
        File targetFile = FileUtil.prepareFile(userFolderPath + "/" + type + name);
        file.transferTo(targetFile);

        return Result.success("云端上传成功");
    }

    /**
     * 读取文件
     */
    @Log
    @GetMapping("/download")
    public Result downloadCloud(String user, String fileName) {
        String userFolderPath = cloudBasePath + "/" + user;
        File targetFile = FileUtil.getFile(userFolderPath + "/" + fileName);
        // 文件不存在
        if (targetFile == null) {
            return Result.error(FileUploadEnum.FILE_NOT_EXIST.getCode(), "文件不存在");
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            LogUtil.logException(this.getClass(), e);
        }

        return Result.success("文件内容", content.toString());
    }

    /**
     * 清除过期文件
     */
    @Scheduled(cron = "0 0 0 */1 * ?")
    protected void clean() {
        File cloudFolder = FileUtil.prepareDirectory(cloudBasePath);
        File[] userFolders = cloudFolder.listFiles();
        if (userFolders == null) return;
        for (File userFolder : userFolders) {
            if (userFolder.isDirectory()) {
                File[] userFiles = userFolder.listFiles();
                if (userFiles == null) continue;
                for (File userFile : userFiles) {
                    if (!userFile.isDirectory()) {
                        // 只删除录像文件
                        if (userFile.getName().startsWith(countType)) {
                            try {
                                // 获取文件的基本属性
                                BasicFileAttributes attrs = Files.readAttributes(userFile.toPath(), BasicFileAttributes.class);
                                // 获取文件创建时间
                                Instant creationTime = attrs.creationTime().toInstant();
                                // 判断是否超过有效期
                                long daysBetween = ChronoUnit.DAYS.between(creationTime, Instant.now());
                                if (daysBetween > expirationDays) userFile.delete();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else FileUtil.forceDeleteFolder(userFile);
                }
            }
            else userFolder.delete();
        }
    }

}
