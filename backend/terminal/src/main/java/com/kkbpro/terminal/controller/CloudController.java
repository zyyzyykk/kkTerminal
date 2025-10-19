package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.enums.FileStateEnum;
import com.kkbpro.terminal.enums.ResultCodeEnum;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.LogUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
        String folderPath = cloudBasePath + "/" + user;
        File folder = new File(folderPath);
        // 如果文件夹不存在则创建
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (folder.listFiles().length > maxCount && countType.equals(type))
            return Result.error(ResultCodeEnum.CLOUD_COUNT_ERROR.getState(),"云端文件过多");
        File aimFile = new File(folderPath + "/" + type + name);
        // 如果文件存在则删除
        if (aimFile.exists()) {
            aimFile.delete();
        }
        file.transferTo(aimFile);

        return Result.success("云端上传成功");
    }

    /**
     * 读取文件
     */
    @Log
    @GetMapping("/download")
    public Result downloadCloud(HttpServletResponse response, String user, String fileName) throws IOException {
        String folderPath = cloudBasePath + "/" + user;
        File file = new File(folderPath + "/" + fileName);
        // 文件不存在
        if (!file.exists()) return Result.error(FileStateEnum.FILE_NOT_EXIST.getState(), "文件不存在");
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
        File cloudBaseFolder = new File(cloudBasePath);
        if (!cloudBaseFolder.exists()) return;
        File[] userFolders = cloudBaseFolder.listFiles();
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
                    else userFile.delete();
                }
            }
            else userFolder.delete();
        }
    }

}
