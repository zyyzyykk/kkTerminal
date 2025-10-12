package com.kkbpro.terminal.config;

import com.kkbpro.terminal.utils.StringUtil;
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
    private Integer sshMaxTimeout;

    /**
     * websocket最大空闲超时 ms
     */
    @Value("${kk.max-idle-timeout:}")
    private Integer maxIdleTimeout;

    /**
     * PC端启用窗口
     */
    @Value("${kk.pc.window:}")
    private Boolean pcWindowTag;

    /**
     * 存储密钥
     */
    @Value("${kk.aes.storage:}")
    private String storageKey;

    /**
     * 登录密码
     */
    @Value("${kk.login.password:}")
    private String password;

    public String getPassword() {
        return StringUtil.isEmpty(password) ? null : password.substring(0, Math.min(password.length(), 16));
    };

}
