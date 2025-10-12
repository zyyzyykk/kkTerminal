package com.kkbpro.terminal.enums;

import lombok.Getter;

@Getter
public enum SocketMessageEnum {

    USER_TEXT(0, "User input text"),

    SIZE_CHANGE(1, "Change the size of virtual terminal"),

    HEART_BEAT(2, "Do heartbeat renewal");

    private final Integer state;

    private final String desc;

    SocketMessageEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

}
