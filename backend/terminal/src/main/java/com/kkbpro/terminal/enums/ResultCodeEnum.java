package com.kkbpro.terminal.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    CLOUD_COUNT_ERROR(506, "云端文件过多"),

    PASSWORD_INCORRECT(507, "密码错误"),

    SSH_NOT_EXIST(602, "ssh连接断开");

    private final Integer state;

    private final String desc;

    ResultCodeEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

}