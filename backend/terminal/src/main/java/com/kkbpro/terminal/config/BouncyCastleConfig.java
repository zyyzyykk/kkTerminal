package com.kkbpro.terminal.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.annotation.PostConstruct;
import java.security.Security;

public class BouncyCastleConfig {

    @PostConstruct
    public void setup() {
        // 添加Bouncy Castle作为安全提供程序（适配MacOS开发）
        Security.addProvider(new BouncyCastleProvider());
    }

}
