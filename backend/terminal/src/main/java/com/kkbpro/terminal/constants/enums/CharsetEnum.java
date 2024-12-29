package com.kkbpro.terminal.constants.enums;

public enum CharsetEnum {

    UTF_8("en_US.UTF-8", "UTF-8"),

    ISO_8859_1("en_US.ISO-8859-1", "ISO-8859-1"),

    ISO_8859_15("en_US.ISO-8859-15", "ISO-8859-15"),

    US_ASCII("C", "US-ASCII"),

    GB18030("zh_CN.GB18030", "GB18030"),

    GBK("zh_CN.GBK", "GBK"),

    GB2312("zh_CN.GB2312", "GB2312"),

    EUC_JP("ja_JP.EUC-JP", "EUC-JP"),

    EUC_KR("ko_KR.EUC-KR", "EUC-KR"),

    KOI8_R("ru_RU.KOI8-R", "KOI8-R"),

    WINDOWS_1251("ru_RU.CP1251", "Windows-1251"),

    WINDOWS_1252("en_US.CP1252", "Windows-1252"),

    ISO_8859_6("ar_SA.ISO-8859-6", "ISO-8859-6"),

    ISO_8859_8("he_IL.ISO-8859-8", "ISO-8859-8"),

    TIS_620("th_TH.TIS-620", "TIS-620"),

    VISCII("vi_VN.VISCII", "VISCII");

    private final String linuxCharset;

    private final String javaCharset;

    CharsetEnum(String linuxCharset, String javaCharset) {
        this.linuxCharset = linuxCharset;
        this.javaCharset = javaCharset;
    }

    public String getLinuxCharset() {
        return linuxCharset;
    }

    public String getJavaCharset() {
        return javaCharset;
    }

    public static CharsetEnum getByLinuxCharset(String linuxCharset) {
        for (CharsetEnum item : CharsetEnum.values()) {
            if (item.getLinuxCharset().equalsIgnoreCase(linuxCharset.trim())) {
                return item;
            }
        }
        return UTF_8;
    }

}
