package com.kkbpro.terminal.constants.enums;

public enum FileBlockStateEnum {

    FILE_UPLOADING(202,"文件后台上传中"),

    CHUNK_UPLOAD_SUCCESS(203,"文件片上传成功"),

    UPLOAD_ERROR(502,"文件片上传失败"),

    UPLOAD_CHUNK_LOST(503,"文件片缺失"),

    UPLOAD_SIZE_DIFF(504,"上传文件大小不一致"),

    SSH_NOT_EXIST(602,"ssh连接断开");

    private Integer state;

    private String desc;

    FileBlockStateEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static FileBlockStateEnum getByState(Integer state) {
        for (FileBlockStateEnum item : FileBlockStateEnum.values()) {
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
