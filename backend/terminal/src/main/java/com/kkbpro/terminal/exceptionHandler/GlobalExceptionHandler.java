package com.kkbpro.terminal.exceptionHandler;


import com.kkbpro.terminal.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MyException.class)  // 捕获自定义异常
    public Result myEx(MyException ex)
    {
        ex.printStackTrace();
        return ex.getResult();
    }

}
