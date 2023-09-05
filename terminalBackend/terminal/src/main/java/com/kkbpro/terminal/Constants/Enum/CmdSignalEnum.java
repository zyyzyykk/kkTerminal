package com.kkbpro.terminal.Constants.Enum;

public enum CmdSignalEnum {
    CTRL_C(3,"ctrl+c");

    private Integer state;
    private String desc;

    CmdSignalEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static CmdSignalEnum getByState(Integer state) {
        for (CmdSignalEnum item : CmdSignalEnum.values()) {
            if (item.getState().equals(state)) {
                return item;
            }
        }
        return null;
    }

    public static CmdSignalEnum getByDesc(String desc) {
        for (CmdSignalEnum item : CmdSignalEnum.values()) {
            if (item.getDesc().equals(desc)) {
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
