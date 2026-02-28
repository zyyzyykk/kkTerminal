package com.kkbpro.terminal.enums;

import lombok.Getter;

@Getter
public enum SocketMessageEnum {

    USER_TEXT(0, "User input text"),

    SIZE_CHANGE(1, "Change the size of virtual terminal"),

    HEART_BEAT(2, "Do heartbeat renewal");

    private final Integer type;

    private final String desc;

    SocketMessageEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
