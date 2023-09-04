package com.kkbpro.terminal.Constants.Enum;

public enum ResultCodeEnum {

    CONNECT_FAIL(-1,"连接服务器失败！"),

    CONTENT_NOT_SHOW(0,"消息不展示"),

    CONTENT_SHOW(1,"消息展示在终端"),
    LINE_PREFIX(2,"命令行前缀");

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
