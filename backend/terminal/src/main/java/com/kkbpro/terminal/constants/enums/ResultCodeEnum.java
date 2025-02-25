package com.kkbpro.terminal.constants.enums;

public enum ResultCodeEnum {

    COOPERATE_KEY_INVALID(-2, "Cooperation Key is Invalid"),

    CONNECT_FAIL(-1,"Fail to connect remote server"),

    CONNECT_SUCCESS(0,"Connecting success"),

    OUT_TEXT(1,"Text output to terminal"),

    COOPERATE_NUMBER_UPDATE(2,"Update cooperate number");

    private Integer state;

    private String desc;

    ResultCodeEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static ResultCodeEnum getByState(Integer state) {
        for (ResultCodeEnum item : ResultCodeEnum.values()) {
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
