package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.controller.CloudController;
import org.semver4j.Semver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

public class CompatibilityUtil {

    private static final String versionFilePath = FileUtil.basePath + "version";

    private static String getCurrentVersion() {
        File versionFile = FileUtil.getFile(versionFilePath);
        if (versionFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
                return reader.readLine();
            } catch (IOException e) {
                LogUtil.logException(CompatibilityUtil.class, e);
            }
        }

        return null;
    }

    private static void setCurrentVersion(String version) {
        File versionFile = FileUtil.prepareFile(versionFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(versionFile))) {
            writer.write(version);
        } catch (IOException e) {
            LogUtil.logException(CompatibilityUtil.class, e);
        }
    }

    private static Semver toSemver(String version) {
        try {
            return new Semver(version);
        } catch (Exception e) {
            return new Semver("0.0.0");
        }
    }

    public static void setupCompatFixes(String latestVersionStr) {
        Semver latestVersion = toSemver(latestVersionStr);
        Semver currentVersion = toSemver(getCurrentVersion());
        if (latestVersion.isEqualTo(currentVersion)) return;
        for (Map.Entry<String, Runnable> fixItem : sortedFixesChain) {
            Semver fixVersion = toSemver(fixItem.getKey());
            if (currentVersion.isLowerThanOrEqualTo(fixVersion)) {
                fixItem.getValue().run();
            }
        }
        setCurrentVersion(latestVersionStr);
    }

    // 兼容3.8.0及以下版本
    private static void fixFor380() {
        File cloudFolder = FileUtil.getDirectory(FileUtil.basePath + "cloud" + "/");
        if (cloudFolder == null) return;
        try {
            File userBaseFolder = FileUtil.prepareDirectory(CloudController.userBasePath);
            Files.move(Paths.get(cloudFolder.getPath()), Paths.get(userBaseFolder.getPath()), StandardCopyOption.REPLACE_EXISTING);
            File[] userFolders = userBaseFolder.listFiles();
            if (userFolders == null) return;
            for (File userFolder : userFolders) {
                if (userFolder.isDirectory()) {
                    File[] userFiles = userFolder.listFiles();
                    if (userFiles == null) continue;
                    for (File userFile : userFiles) {
                        if (!userFile.isDirectory()) {
                            String folderPath = CloudController.dataPath;
                            if (userFile.getName().startsWith(CloudController.recordPrefix)) {
                                folderPath = CloudController.recordPath;
                            }
                            File targetFile = FileUtil.prepareFile(userFolder.getPath() + folderPath + userFile.getName());
                            Files.move(Paths.get(userFile.getPath()), Paths.get(targetFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.logException(CompatibilityUtil.class, e);
        }
    }

    private static final List<Map.Entry<String, Runnable>> sortedFixesChain = Stream.<Map.Entry<String, Runnable>>of(
            new AbstractMap.SimpleEntry<String, Runnable>("3.8.0", CompatibilityUtil::fixFor380))
            .sorted(Comparator.comparing(entry -> toSemver(entry.getKey())))
            .toList();

}
