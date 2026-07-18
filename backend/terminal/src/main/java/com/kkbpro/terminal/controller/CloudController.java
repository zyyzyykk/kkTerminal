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

    public static final String userBasePath = FileUtil.basePath + "user" + "/";

    public static final String dataPath = "/" + "data" + "/";

    public static final String recordPath = "/" + "record" + "/";

    public static final String recordPrefix = "record-";

    private static final Integer recordMaxCount = 100;

    private static final Integer recordExpirationDays = 7;

    /**
     * 上传文件
     */
    @Log
    @PostMapping("/upload")
    public Result uploadCloud(String user, String type, String name, MultipartFile file) {
        String fileFolderPath = userBasePath + user + (recordPrefix.equals(type) ? recordPath : dataPath);
        // 限制录像文件数量
        if (recordPrefix.equals(type)) {
            File recordFolder = FileUtil.prepareDirectory(fileFolderPath);
            File[] recordFiles = recordFolder.listFiles();
            if (recordFiles != null && recordFiles.length > recordMaxCount) {
                return Result.error(ResultCodeEnum.RECORD_COUNT_EXCEEDED.getCode(), "录像文件过多");
            }
        }
        try {
            File targetFile = FileUtil.prepareFile(fileFolderPath + type + name);
            file.transferTo(targetFile);
        } catch (IOException e) {
            LogUtil.logException(this.getClass(), e);
        }

        return Result.success("云端上传成功");
    }

    /**
     * 读取文件
     */
    @Log
    @GetMapping("/download")
    public Result downloadCloud(String user, String fileName) {
        String fileFolderPath = userBasePath + user + ((fileName != null && fileName.startsWith(recordPrefix)) ? recordPath : dataPath);
        File targetFile = FileUtil.getFile(fileFolderPath + fileName);
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
        File userBaseFolder = FileUtil.prepareDirectory(userBasePath);
        File[] userFolders = userBaseFolder.listFiles();
        if (userFolders == null) return;
        for (File userFolder : userFolders) {
            if (userFolder.isDirectory()) {
                File recordFolder = FileUtil.prepareDirectory(userFolder.getPath() + recordPath);
                File[] recordFiles = recordFolder.listFiles();
                if (recordFiles == null) continue;
                for (File recordFile : recordFiles) {
                    if (!recordFile.isDirectory()) {
                        try {
                            // 获取文件的基本属性
                            BasicFileAttributes attrs = Files.readAttributes(recordFile.toPath(), BasicFileAttributes.class);
                            // 获取文件创建时间
                            Instant creationTime = attrs.creationTime().toInstant();
                            // 判断是否超过有效期
                            long daysBetween = ChronoUnit.DAYS.between(creationTime, Instant.now());
                            if (daysBetween > recordExpirationDays) recordFile.delete();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

}
