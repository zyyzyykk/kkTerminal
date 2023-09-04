package com.kkbpro.terminal.GlobalExceptionHandler;

import com.kkbpro.terminal.Result.Result;
import lombok.Data;

@Data
public class MyException extends RuntimeException{

    private Result result;

    public MyException(String message){
        super(message);
    }
}
