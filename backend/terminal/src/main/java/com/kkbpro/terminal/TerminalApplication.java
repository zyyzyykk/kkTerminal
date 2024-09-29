package com.kkbpro.terminal;

import com.kkbpro.terminal.controller.SystemController;
import com.kkbpro.terminal.utils.FileUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class TerminalApplication {
    private static final Properties properties;
    static {
        properties = new Properties();
        InputStream inputStream = TerminalApplication.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanData() {
        // 清除文件数据残留
        File data = new File(FileUtil.folderBasePath);
        if(data.exists()) FileUtil.tmpFloderDelete(data);
    }
    public static void main(String[] args) {
        // 清除文件数据残留
        File data = new File(FileUtil.folderBasePath);
        if(data.exists()) FileUtil.tmpFloderDelete(data);
        data.mkdirs();
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
        if(Boolean.parseBoolean(properties.getProperty("kk.pc.window")))
            openWebPage(enableOnceMonitor);
    }

    private static void setServerOS() {
        // 支持PC端：Windows和Mac
        String os = System.getProperty("os.name").toLowerCase();
        // e.g. mac os x
        if (os.contains("mac")) {
            SystemController.serverOS = "Mac";
        }
        // e.g. windows 10
        else if (os.contains("win")) {
            SystemController.serverOS = "Windows";
        }
        else {
            SystemController.serverOS = "Linux";
        }
    }
    private static void openWebPage(boolean enableOnceMonitor) {
        Runtime runtime = Runtime.getRuntime();
        try {
            // e.g. mac os x
            if ("Mac".equals(SystemController.serverOS)) {
                runtime.exec("open " + getUrl());
                if(enableOnceMonitor) startOnceMonitor();
                else System.exit(0); // 结束进程
            }
            // e.g. windows 10
            else if ("Windows".equals(SystemController.serverOS)) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + getUrl());
                if(enableOnceMonitor) startOnceMonitor();
                else System.exit(0); // 结束进程
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String getUrl() {
        return "http://localhost:" + properties.getProperty("server.port");
    }


    private static void startOnceMonitor() {
        new Thread(() -> {
            try {
                Thread.sleep(1000 * 18);
                if(!SystemController.isEnableMonitor()) System.exit(0); // 结束进程
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
