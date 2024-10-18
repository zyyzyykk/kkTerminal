package com.kkbpro.terminal.constants.enums;


public enum MessageInfoTypeRnum {

    USER_TEXT(0,"用户输入的文本"),

    SIZE_CHANGE(1,"改变虚拟终端大小"),


    HEART_BEAT(2,"进行心跳续约");

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
