package com.kkbpro.terminal;

import com.kkbpro.terminal.controller.SystemController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    public static void main(String[] args) {
        // 打开Web浏览器
        if(Boolean.parseBoolean(properties.getProperty("kk.pc.window")))
            openWebPage();
        SpringApplication.run(TerminalApplication.class, args);
    }

    private static void openWebPage() {
        // 支持PC端：Windows和Mac
        String os = System.getProperty("os.name").toLowerCase();
        Runtime runtime = Runtime.getRuntime();
        try {
            // e.g. mac os x
            if (os.contains("mac")) {
                runtime.exec("open " + getUrl());
                SystemController.serverOS = "Mac";
                startOnceMonitor();
            }
            // e.g. windows 10
            else if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + getUrl());
                SystemController.serverOS = "Windows";
                startOnceMonitor();
            }
            else {
                SystemController.serverOS = "Linux";
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
                Thread.sleep(1000 * 6);
                if(!SystemController.isEnableMonitor()) System.exit(0); // 结束进程
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
