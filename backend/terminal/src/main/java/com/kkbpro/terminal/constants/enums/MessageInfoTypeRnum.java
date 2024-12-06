package com.kkbpro.terminal.constants.enums;

public enum MessageInfoTypeRnum {

    USER_TEXT(0,"User input text"),

    SIZE_CHANGE(1,"Change the size of virtual terminal"),

    HEART_BEAT(2,"Do heartbeat renewal");

    private Integer state;

    private String desc;

    MessageInfoTypeRnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static MessageInfoTypeRnum getByState(Integer state) {
        for (MessageInfoTypeRnum item : MessageInfoTypeRnum.values()) {
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
