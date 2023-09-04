package com.kkbpro.terminal.GlobalExceptionHandler;

import com.kkbpro.terminal.Result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)  // 捕获所有异常
    public Result ex(Exception ex)
    {
        ex.printStackTrace();
        return Result.setError(-1,"操作失败，请联系管理员");
    }

    @ExceptionHandler(MyException.class)  // 捕获自定义异常
    public Result myEx(MyException ex)
    {
        ex.printStackTrace();
        return ex.getResult();
    }
}
