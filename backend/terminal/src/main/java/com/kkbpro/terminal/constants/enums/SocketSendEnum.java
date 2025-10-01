package com.kkbpro.terminal.constants.enums;

public enum SocketSendEnum {

    COOPERATE_KEY_INVALID(-2, "Cooperation Key is Invalid"),

    CONNECT_FAIL(-1, "Fail to connect remote server"),

    CONNECT_SUCCESS(0, "Connecting success"),

    OUT_TEXT(1, "Text output to terminal"),

    COOPERATE_NUMBER_UPDATE(2, "Update cooperate number"),

    FILE_TRANSPORT_UPDATE(3, "Update file transport");

    private final Integer state;

    private final String desc;

    SocketSendEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static SocketSendEnum getByState(Integer state) {
        for (SocketSendEnum item : SocketSendEnum.values()) {
            if (item.getState().equals(state)) {
                return item;
            }
        }
        return null;
    }

    public Integer getState() {
        return state;
    }

    public String getDesc() {
        return desc;
    }

}
