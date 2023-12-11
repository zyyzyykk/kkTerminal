package com.kkbpro.terminal.Pojo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadInfo {

    private MultipartFile file;

    private Integer chunks;

    private Integer chunk;

    private String id;

    private String sshKey;

    private String path;

}