package com.kkbpro.terminal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 请求重定向
 */
@Controller
public class MyErrorController implements ErrorController {

    @GetMapping({"/error", "/index.html/", "/index.html//**"})
    public String handleError(HttpServletRequest request) {
        String targetUrl = UriComponentsBuilder.fromPath("/")
                .query(request.getQueryString())
                .build()
                .toUriString();

        return "redirect:" + targetUrl;
    }
}