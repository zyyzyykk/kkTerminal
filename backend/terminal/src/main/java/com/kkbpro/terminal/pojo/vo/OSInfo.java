package com.kkbpro.terminal.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统信息
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSInfo {

    private String serverOS;

    private String clientOS;

    private String windowId;

}
