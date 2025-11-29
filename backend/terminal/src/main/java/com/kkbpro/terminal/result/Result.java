package com.kkbpro.terminal.result;

import com.alibaba.fastjson.JSON;
import com.kkbpro.terminal.enums.ResultStatusEnum;
import com.kkbpro.terminal.utils.AESUtil;
import com.kkbpro.terminal.utils.I18nUtil;
import com.kkbpro.terminal.utils.LogUtil;
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

    private static Result newInstance(String status, Integer code, String info, String data) {
        Result result = new Result();
        result.setStatus(status);
        result.setCode(code);
        result.setInfo(info);
        try {
            result.setData(data == null ? null : AESUtil.encrypt(data));
        } catch (Exception e) {
            LogUtil.logException(Result.class, e);
            result.setData(null);
        }

        return result;
    }

    // 警告返回
    private static Result warningStr(Integer code, String info, String data) {
        return newInstance(ResultStatusEnum.WARNING.getStatus(), code, info, data);
    }

    public static Result warning(Integer code, String info) {
        return Result.warningStr(code, info, null);
    }

    // 成功返回
    private static Result successStr(Integer code, String info, String data) {
        return newInstance(ResultStatusEnum.SUCCESS.getStatus(), code, info, data);
    }

    public static Result success(String info, Object data) {
        return Result.successStr(200, info, data == null ? null : JSON.toJSONString(data));
    }

    public static Result success(Integer code, String info) {
        return Result.successStr(code, info, null);
    }

    public static Result success(String info) {
        return Result.successStr(200, info, null);
    }

    // 错误返回
    private static Result errorStr(Integer code, String info, String data) {
        return newInstance(ResultStatusEnum.ERROR.getStatus(), code, info, data);
    }

    public static Result error(String info, Object data) {
        return Result.errorStr(500, info, data == null ? null : JSON.toJSONString(data));
    }

    public static Result error(Integer code, String info) {
        return Result.errorStr(code, info, null);
    }

    public static Result error(String info) {
        return Result.errorStr(500, info, null);
    }

}