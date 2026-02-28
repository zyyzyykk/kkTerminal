package com.kkbpro.terminal;

import com.kkbpro.terminal.controller.SystemController;
import com.kkbpro.terminal.enums.OperatingSystemEnum;
import com.kkbpro.terminal.utils.FileUtil;
import com.kkbpro.terminal.utils.LogUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@EnableScheduling
@SpringBootApplication
public class TerminalApplication {

    public static final Properties properties;

    static {
        properties = new Properties();
        InputStream inputStream = TerminalApplication.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LogUtil.logException(TerminalApplication.class, e);
        }
    }

    public static void cleanTempData() {
        // 清除临时数据
        File tempFolder = FileUtil.getDirectory(FileUtil.tempBasePath);
        if (tempFolder != null) {
            FileUtil.forceDeleteFolder(tempFolder);
        }
    }

    @PreDestroy
    public void preDestroy() {
        cleanTempData();
    }

    public static void main(String[] args) {
        // 重置临时数据
        cleanTempData();
        FileUtil.prepareDirectory(FileUtil.tempBasePath);
        // 设置服务器OS
        setServerOS();
        // 启动后端
        boolean enableOnceMonitor = true;
        try {
            SpringApplication.run(TerminalApplication.class, args);
        } catch (Exception e) {
            enableOnceMonitor = false;
        }
        // 打开Web浏览器
        if (Boolean.parseBoolean(properties.getProperty("kk.pc.window"))) {
            openWebPage(enableOnceMonitor);
        }
    }

    private static void setServerOS() {
        // 支持本地运行
        String os = System.getProperty("os.name").toLowerCase();
        // mac
        if (os.contains("mac")) {
            SystemController.serverOS = OperatingSystemEnum.MAC.getName();
        }
        // windows
        else if (os.contains("win")) {
            SystemController.serverOS = OperatingSystemEnum.WINDOWS.getName();
        }
        else {
            SystemController.serverOS = OperatingSystemEnum.LINUX.getName();
        }
    }

    private static void openWebPage(boolean enableOnceMonitor) {
        Runtime runtime = Runtime.getRuntime();
        try {
            // mac
            if (OperatingSystemEnum.MAC.getName().equals(SystemController.serverOS)) {
                runtime.exec("open " + getUrl());
                if (enableOnceMonitor) startOnceMonitor();
                else System.exit(0); // 结束进程
            }
            // windows
            else if (OperatingSystemEnum.WINDOWS.getName().equals(SystemController.serverOS)) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + getUrl());
                if (enableOnceMonitor) startOnceMonitor();
                else System.exit(0); // 结束进程
            }
        } catch (Exception e) {
            LogUtil.logException(TerminalApplication.class, e);
        }
    }

    private static String getUrl() {
        return "http://localhost:" + properties.getProperty("server.port");
    }

    private static void startOnceMonitor() {
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(1000 * 18);
                if (!SystemController.isEnableMonitor()) System.exit(0); // 结束进程
            } catch (InterruptedException e) {
                LogUtil.logException(TerminalApplication.class, e);
            }
        });
    }

}
