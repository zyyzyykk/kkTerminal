package com.kkbpro.terminal.result;

import com.alibaba.fastjson.JSON;
import com.kkbpro.terminal.utils.AesUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 返回结果的信息类
 */
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    // 状态
    private String status;

    // 响应码
    private Integer code;

    // 状态信息
    private String info;

    // 返回数据
    private String data;

    // 失败返回
    public static Result setFail(Integer code, String info)
    {
        Result result = new Result();
        result.setStatus("warning");
        result.setCode(code);
        result.setInfo(info);
        result.setData(null);

        return result;
    }

    // 成功返回
    public static Result setSuccess(Integer code, String info, Object data) {
        Result result = new Result();
        result.setStatus("success");
        result.setCode(code);
        result.setInfo(info);
        try {
            if(null == data) result.setData(null);
            else result.setData(AesUtil.aesEncrypt(JSON.toJSONString(data)));
        } catch (Exception e) {
            System.out.println("加密异常");
            result.setData(null);
        }

        return result;
    }


    public static Result setSuccess(String info, Object data) {
        return Result.setSuccess(200, info, data);
    }

    // 错误返回
    public static Result setError(Integer code,String info)
    {
        Result result = new Result();
        result.setStatus("error");
        result.setCode(code);
        result.setInfo(info);
        result.setData(null);

        return result;
    }


    public static Result setError(Integer code, String info, Object data) {
        Result result = new Result();
        result.setStatus("error");
        result.setCode(code);
        result.setInfo(info);
        try {
            if(null == data) result.setData(null);
            else result.setData(AesUtil.aesEncrypt(JSON.toJSONString(data)));
        } catch (Exception e) {
            System.out.println("加密异常");
            result.setData(null);
        }

        return result;
    }


}
