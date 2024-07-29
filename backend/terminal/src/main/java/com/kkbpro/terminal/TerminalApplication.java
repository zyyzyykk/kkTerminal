package com.kkbpro.terminal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class TerminalApplication {
    public static void main(String[] args) {
        // 打开Web浏览器
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
            }
            // e.g. windows 10
            else if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + getUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String getUrl() {
        String baseUrl = "http://localhost:";
        Properties properties = new Properties();
        InputStream inputStream = TerminalApplication.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(inputStream);
            return baseUrl + properties.getProperty("server.port");
        } catch (IOException e) {
            e.printStackTrace();
            return baseUrl + "3000";
        }
    }

}
