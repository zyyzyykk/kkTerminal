package com.kkbpro.terminal.pojo.dto;

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

    private PrivateKey server_key;

    private Integer authType = 0;

    private String lang = "en";

}
