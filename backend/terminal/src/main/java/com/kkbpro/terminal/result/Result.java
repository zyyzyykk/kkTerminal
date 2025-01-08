package com.kkbpro.terminal.result;

import com.alibaba.fastjson.JSON;
import com.kkbpro.terminal.utils.AesUtil;
import com.kkbpro.terminal.utils.I18nUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 返回结果的信息类
 */
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

    // 国际化info属性
    public void setInfo(String info) {
        this.info = I18nUtil.getMessage(info);
    }

    // 失败返回
    public static Result fail(Integer code, String info)
    {
        Result result = new Result();
        result.setStatus("warning");
        result.setCode(code);
        result.setInfo(info);
        result.setData(null);

        return result;
    }

    // 成功返回
    public static Result successStr(Integer code, String info, String data) {
        Result result = new Result();
        result.setStatus("success");
        result.setCode(code);
        result.setInfo(info);
        try {
            if(null == data) result.setData(null);
            else result.setData(AesUtil.aesEncrypt(data));
        } catch (Exception e) {
            System.out.println("Encrypt Error");
            result.setData(null);
        }

        return result;
    }
    public static Result success(Integer code, String info, Object data) {
       return Result.successStr(code, info, JSON.toJSONString(data));
    }

    public static Result success(String info, Object data) {
        return Result.success(200, info, data);
    }

    public static Result success(String info) {
        return Result.success(200, info, null);
    }

    // 错误返回
    public static Result error(Integer code, String info, Object data) {
        Result result = new Result();
        result.setStatus("error");
        result.setCode(code);
        result.setInfo(info);
        try {
            if(null == data) result.setData(null);
            else result.setData(AesUtil.aesEncrypt(JSON.toJSONString(data)));
        } catch (Exception e) {
            System.out.println("Encrypt Error");
            result.setData(null);
        }

        return result;
    }

    public static Result error(Integer code, String info) {
        return Result.error(code, info, null);
    }

    public static Result error(String info) {
        return Result.error(500, info);
    }

}