package com.kkbpro.terminal.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    // 文件名
    private String name;

    // 是否为文件夹
    private Boolean type;

}