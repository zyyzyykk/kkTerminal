package com.kkbpro.terminal.utils;

import com.kkbpro.terminal.constants.enums.FileBlockStateEnum;
import com.kkbpro.terminal.exception.MyException;
import com.kkbpro.terminal.result.Result;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    public static final String folderBasePath = System.getProperty("user.dir") + "/data";

    /**
     * 文件片合并
     */
    public static void fileChunkMerge(String folderPath, String fileName, Integer chunks, Long totalSize) {
        File folder = new File(folderPath);
        // 合并的文件
        File finalFile = new File(folderPath + "/" + fileName);
        // 获取暂存切片文件的文件夹中的所有文件
        File[] files = folder.listFiles();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        if(files != null)
        {
            try {
                outputStream = new FileOutputStream(finalFile, true);

                List<File> list = new ArrayList<>();
                for (File file : files) {
                    // 判断是否是文件对应的文件片
                    if (StringUtil.isFileChunk(file.getName(),chunks,fileName)) {
                        list.add(file);
                    }
                }
                // 如果服务器上的切片数量和前端给的数量不匹配
                if (chunks != list.size()) {
                    MyException myException = new MyException("文件片缺失");
                    myException.setResult(Result.setError(FileBlockStateEnum.UPLOAD_CHUNK_LOST.getState(), "文件片缺失"));
                    throw myException;
                }
                // 根据切片文件的下标进行排序
                List<File> fileListCollect = list.parallelStream().sorted(((file1, file2) -> {
                    Integer chunk1 = StringUtil.getFileChunkIndex(file1.getName());
                    Integer chunk2 = StringUtil.getFileChunkIndex(file2.getName());
                    return chunk1 - chunk2;
                })).collect(Collectors.toList());
                // 根据排序的顺序依次将文件合并到新的文件中
                for (File file : fileListCollect) {
                    inputStream = new FileInputStream(file);
                    int len = 0;
                    byte[] bytes = new byte[2 * 1024 * 1024];
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                    inputStream.close();
                    outputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outputStream != null) outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 产生的文件大小和前端一开始上传的文件不一致
        if (finalFile.length() != totalSize) {
            MyException myException = new MyException("上传文件大小不一致");
            myException.setResult(Result.setError(FileBlockStateEnum.UPLOAD_SIZE_DIFF.getState(), "上传文件大小不一致"));
            throw myException;
        }

    }

    /**
     * 删除文件夹
     */
    public static void tmpFloderDelete(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    tmpFloderDelete(file);
                }
            }
        }
        directory.delete();
    }

}
