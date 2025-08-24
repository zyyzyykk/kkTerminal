package com.kkbpro.terminal.constants.enums;

public enum SocketMessageEnum {

    USER_TEXT(0,"User input text"),

    SIZE_CHANGE(1,"Change the size of virtual terminal"),

    HEART_BEAT(2,"Do heartbeat renewal");

    private Integer state;

    private String desc;

    SocketMessageEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static SocketMessageEnum getByState(Integer state) {
        for (SocketMessageEnum item : SocketMessageEnum.values()) {
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
