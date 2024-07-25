package com.kkbpro.terminal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class TerminalApplication {

    public static void main(String[] args) {

        SpringApplication.run(TerminalApplication.class, args);
        // 支持PC端：Windows和Mac
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("win") || osName.contains("mac")) {
            // 打开Web浏览器
            openWebPage("http://localhost:3000");
        }
    }

    private static void openWebPage(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("Open browser error.");
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot open browser automatically.");
        }
    }

}
