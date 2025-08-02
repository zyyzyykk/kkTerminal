package com.kkbpro.terminal.exception;


import com.kkbpro.terminal.result.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义异常，解决参数校验问题
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