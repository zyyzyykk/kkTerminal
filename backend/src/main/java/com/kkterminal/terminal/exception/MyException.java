package com.kkterminal.terminal.exception;


import com.kkterminal.terminal.result.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义异常
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MyException extends RuntimeException {

    private Result result;

    public MyException(Result result) {
        super(result.getInfo());
        this.result = result;
    }
}