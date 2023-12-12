package com.kkbpro.terminal.Constants.Enum;

public enum FileUploadStateEnum {

    FILE_UPLOAD_SUCCESS(202,"文件上传成功"),

    CHUNK_UPLOAD_SUCCESS(203,"文件片上传成功"),

    UPLOAD_ERROR(502,"文件片上传失败"),

    UPLOAD_CHUNK_LOST(502,"文件片缺失"),

    UPLOAD_SIZE_DIFF(503,"上传文件大小不一致");

    private Integer state;

    private String desc;

    FileUploadStateEnum(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static FileUploadStateEnum getByState(Integer state) {
        for (FileUploadStateEnum item : FileUploadStateEnum.values()) {
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
