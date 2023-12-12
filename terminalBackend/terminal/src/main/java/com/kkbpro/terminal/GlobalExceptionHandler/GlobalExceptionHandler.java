package com.kkbpro.terminal.GlobalExceptionHandler;


import com.kkbpro.terminal.Result.Result;
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
