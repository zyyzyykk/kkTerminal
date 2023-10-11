package com.kkbpro.terminal.Constants.Enum;

public enum ResultCodeEnum {
    CONNECT_FAIL(-1,"连接服务器失败！"),

    CONNECT_SUCCESS(0,"连接服务器成功！"),

    OUT_TEXT(1,"输出到终端屏幕的文本");
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

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
