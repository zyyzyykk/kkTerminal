package com.kkbpro.terminal.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    CLOUD_COUNT_ERROR(506, "云端文件过多"),

    PASSWORD_INCORRECT(507, "密码错误"),

    SSH_NOT_EXIST(602, "ssh连接断开");

    private final Integer code;

    private final String desc;

    ResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}