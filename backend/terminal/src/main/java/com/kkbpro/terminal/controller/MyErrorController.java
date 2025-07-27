package com.kkbpro.terminal.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求重定向
 */
@Controller
public class MyErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        String redirect = "redirect:/";
        // 获取原始请求参数
        String queryString = request.getQueryString();
        // 将参数附加到重定向路径上
        if(queryString != null && !queryString.isEmpty()) redirect += ("?" + queryString);
        return redirect;
    }
}