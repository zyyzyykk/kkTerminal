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
    @Value("${kk.app.welcome:}")
    private String welcome;

    /**
     * github源地址
     */
    @Value("${kk.app.source:}")
    private String source;

    /**
     * 艺术字横幅
     */
    @Value("${kk.app.banner:}")
    private String banner;

    /**
     * 本地PC端窗口
     */
    @Value("${kk.pc.window:}")
    private Boolean pcWindow;

    /**
     * 存储密钥
     */
    @Value("${kk.key.storage:}")
    private String storageKey;

    /**
     * 登录密码
     */
    @Value("${kk.key.password:}")
    private String password;

    public String getPassword() {
        return StringUtil.isEmpty(password) ? null : password.substring(0, Math.min(password.length(), 16));
    };

}
