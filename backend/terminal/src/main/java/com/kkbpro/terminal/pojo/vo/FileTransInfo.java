package com.kkbpro.terminal.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传输文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTransInfo {

    // 文件id
    private String id;

    // 文件所在目录
    private String path;

    // 文件名
    private String name;

    // 文件大小: -1L为文件夹
    private Long size = 0L;

    // 类型: 0等待中/1上传中/2下载中/3已完成
    private Integer index = 0;

    // 状态: 0成功/-1上传失败/-2下载失败
    private Integer status = 0;

}
