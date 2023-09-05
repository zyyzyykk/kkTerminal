package com.kkbpro.terminal.Constants.Enum;

public enum FrontSocketEnum {

    TEXT_CMD(0,"文本命令"),

    CRTL_CMD(1,"快捷键");

    private Integer state;

    private String desc;

    FrontSocketEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static FrontSocketEnum getByState(Integer state) {
        for (FrontSocketEnum item : FrontSocketEnum.values()) {
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
