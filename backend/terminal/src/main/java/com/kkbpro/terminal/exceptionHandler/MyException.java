package com.kkbpro.terminal.exceptionHandler;


import com.kkbpro.terminal.result.Result;
import lombok.Data;

/**
 * 自定义异常，解决参数校验问题
 */
@Data
public class MyException extends RuntimeException{

    private Result result;

    public MyException(String message){
        super(message);
    }
}
