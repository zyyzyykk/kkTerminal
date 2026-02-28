package com.kkbpro.terminal.enums;

import com.kkbpro.terminal.utils.StringUtil;
import lombok.Getter;

@Getter
public enum FileUntarEnum {

    TAR_XZ(".tar.xz", "tar -xvJf "),

    TAR_GZ(".tar.gz", "tar -xvzf "),

    TAR_BZ2(".tar.bz2", "tar -xvjf "),

    TAR(".tar", "tar -xvf "),

    TBZ2(".tbz2", "tar -xvjf "),

    GZ(".gz", "gzip -d "),

    BZ2(".bz2", "bzip2 -d "),

    XZ(".xz", "xz -d "),

    ZIP(".zip", "unzip ");

    private final String suffix;

    private final String command;

    FileUntarEnum(String suffix, String command) {
        this.suffix = suffix;
        this.command = command;
    }

    public static FileUntarEnum getByFileName(String fileName) {
        if (StringUtil.isEmpty(fileName)) return null;
        for (FileUntarEnum item : FileUntarEnum.values()) {
            if (fileName.endsWith(item.getSuffix())) {
                return item;
            }
        }
        return null;
    }

}
