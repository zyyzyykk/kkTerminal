package com.kkbpro.terminal.Config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {

    // 连接的服务器ip
    @Value("${kk.server.ip:}")
    private String serverIP;

    // 连接的服务器端口号
    @Value("${kk.server.port:}")
    private String serverPort;

    // 艺术字标题
    @Value("${kk.title:}")
    private String title;

}
