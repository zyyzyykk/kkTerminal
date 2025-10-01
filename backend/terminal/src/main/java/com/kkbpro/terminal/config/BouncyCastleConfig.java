package com.kkbpro.terminal.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.Security;

@Configuration
public class BouncyCastleConfig {

    @PostConstruct
    public void setup() {
        // 添加BouncyCastle作为安全提供程序
        Security.addProvider(new BouncyCastleProvider());
    }

}
