package com.kkbpro.terminal.exception;


import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.LogUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MyException.class)  // 捕获自定义异常
    public Result myEx(MyException ex) {
        return ex.getResult();
    }
}
