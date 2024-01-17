package com.kkbpro.terminal.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 服务器相关
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvInfo {
    private String server_ip;

    private Integer server_port;

    private String server_user;

    private String server_password;

}
