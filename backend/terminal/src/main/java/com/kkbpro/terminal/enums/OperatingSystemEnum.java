package com.kkbpro.terminal.enums;

import com.kkbpro.terminal.utils.StringUtil;
import lombok.Getter;

@Getter
public enum OperatingSystemEnum {

    WINDOWS("Windows", "windows"),

    MAC("Mac", "mac"),

    LINUX("Linux", "linux"),

    ANDROID("Android", "android"),

    IPHONE("iOS", "iphone"),

    IPAD("iOS", "ipad"),

    UNKNOWN("Unknown", null);

    private final String name;

    private final String ua;

    OperatingSystemEnum(String name, String ua) {
        this.name = name;
        this.ua = ua;
    }

    public static OperatingSystemEnum getOSFromUA(String userAgent) {
        if (StringUtil.isEmpty(userAgent)) return UNKNOWN;
        userAgent = userAgent.toLowerCase();
        for (OperatingSystemEnum item : OperatingSystemEnum.values()) {
            if (item.ua != null) {
                if (userAgent.contains(item.getUa())) {
                    return item;
                }
            }
        }
        return UNKNOWN;
    }

}
