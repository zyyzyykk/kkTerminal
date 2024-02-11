package com.kkbpro.terminal.config;

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

    /**
     * 欢迎语
     */
    @Value("${kk.welcome:}")
    private String welcome;

    /**
     * github源地址
     */
    @Value("${kk.source:}")
    private String source;


    /**
     * 艺术字标题
     */
    @Value("${kk.title:}")
    private String title;

    /**
     * ssh连接最大超时时间 ms
     */
    @Value("${kk.ssh-max-timeout:}")
    private Integer SshMaxTimeout;

    /**
     * websocket最大空闲超时 ms
     */
    @Value("${kk.max-idle-timeout:}")
    private Integer MaxIdleTimeout;


}
