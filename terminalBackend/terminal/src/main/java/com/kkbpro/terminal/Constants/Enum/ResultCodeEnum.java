package com.kkbpro.terminal.Constants.Enum;

public enum ResultCodeEnum {
    CONNECT_FAIL(-1,"连接服务器失败！"),

    CONTENT_NOT_SHOW(0,"消息不展示"),

    KK_SHOW(2,"欢迎语等消息"),

    GET_INIT(3,"初始化"),

    SHELL_EXEC(10,"shell命令消息"),

    SHELL_CMD(11,"shell快捷键消息"),


    SHELL_NO_RETURN(12,"向服务器发送，但不返回消息给前端");



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
