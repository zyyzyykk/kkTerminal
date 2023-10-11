package com.kkbpro.terminal.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvInfo {

    // 服务器相关
    private String server_ip;

    private Integer server_port;

    private String server_user;

    private String server_password;

}
