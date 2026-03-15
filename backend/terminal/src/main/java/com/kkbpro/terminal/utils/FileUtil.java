package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.enums.FileUploadEnum;
import com.kkbpro.terminal.exception.MyException;
import com.kkbpro.terminal.result.Result;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

    public static final String tempBasePath = System.getProperty("user.dir") + "/" + "data" + "/" + "temp";

    /**
     * 获取文件
     */
    public static File getFile(String fullPath) {
        File file = new File(fullPath);
        return file.exists() ? file : null;
    }

    /**
     * 获取文件夹
     */
    public static File getDirectory(String path) {
        return getFile(path);
    }

    /**
     * 准备文件
     */
    public static File prepareFile(String fullPath) {
        File file = new File(fullPath);
        // 如果文件存在则删除
        if (file.exists()) {
            file.delete();
        }
        // 确保父目录存在
        else if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return file;
    }

    /**
     * 准备文件夹
     */
    public static File prepareDirectory(String path) {
        File directory = new File(path);
        // 如果文件夹不存在则创建
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    /**
     * 文件夹强制(递归)删除
     */
    public static void forceDeleteFolder(File parentItem) {
        if (parentItem.isDirectory()) {
            File[] subItems = parentItem.listFiles();
            if (subItems != null) {
                for (File subItem : subItems) {
                    forceDeleteFolder(subItem);
                }
            }
        }
        parentItem.delete();
    }

    /**
     * 合并文件片
     */
    public static void mergeFileChunks(String folderPath, String fileName, Integer chunks, Long totalSize) {
        File folder = FileUtil.getDirectory(folderPath);
        // 合并后的文件
        File mergedFile = FileUtil.prepareFile(folderPath + "/" + fileName);
        // 获取所有文件片
        File[] chunkFiles = folder.listFiles();
        if (chunkFiles == null) {
            throw new MyException(Result.error(FileUploadEnum.UPLOAD_CHUNK_LOST.getCode(), "文件片缺失"));
        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(mergedFile, true))) {
            List<File> filteredChunkFiles = Arrays.stream(chunkFiles)
                    .filter(chunkFile -> isChunkFile(chunkFile.getName(), chunks, fileName))
                    .toList();
            // 文件片数量不匹配
            if (chunks != filteredChunkFiles.size()) {
                throw new MyException(Result.error(FileUploadEnum.UPLOAD_CHUNK_LOST.getCode(), "文件片缺失"));
            }
            // 根据文件片号排序
            List<File> sortedChunkFiles = filteredChunkFiles.parallelStream().sorted(((file1, file2) -> {
                Integer chunk1 = getChunkFileIndex(file1.getName());
                Integer chunk2 = getChunkFileIndex(file2.getName());
                return chunk1 - chunk2;
            })).toList();
            // 依次将文件片合并到新文件
            for (File chunkFile : sortedChunkFiles) {
                try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(chunkFile.toPath()))) {
                    int len;
                    byte[] bytes = new byte[4 * 1024 * 1024];
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            LogUtil.logException(FileUtil.class, e);
            throw new MyException(Result.error(FileUploadEnum.CHUNK_MERGE_ERROR.getCode(), "文件片合并失败"));
        }

        // 合并文件大小不一致
        if (mergedFile.length() != totalSize) {
            throw new MyException(Result.error(FileUploadEnum.UPLOAD_SIZE_DIFF.getCode(), "上传文件大小不一致"));
        }
    }

    /**
     * 判断分片文件名
     */
    private static Boolean isChunkFile(String chunkFileName, Integer chunks, String originFileName) {
        int index = chunkFileName.lastIndexOf("-");
        if (index != -1) {
            String fileName = chunkFileName.substring(0, index);
            if (!originFileName.equals(fileName)) return false;
            try {
                int chunk = Integer.parseInt(chunkFileName.substring(index + 1));
                if (chunk < 1 || chunk > chunks) return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        return false;
    }


    /**
     * 获取文件片片号
     */
    private static Integer getChunkFileIndex(String chunkFileName) {
        int index = chunkFileName.lastIndexOf("-");
        if (index != -1) {
            return Integer.parseInt(chunkFileName.substring(index + 1));
        }

        return 1;
    }

}
