package com.kkbpro.terminal.enums;

import lombok.Getter;

@Getter
public enum ResultStatusEnum {

    SUCCESS("success"),

    WARNING("warning"),

    ERROR("error");

    private final String status;

    ResultStatusEnum(String status) {
        this.status = status;
    }

}
