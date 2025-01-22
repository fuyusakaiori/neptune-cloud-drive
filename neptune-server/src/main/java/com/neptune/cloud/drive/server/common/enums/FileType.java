package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 文件类型: 文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
 */
@Getter
@AllArgsConstructor
public enum FileType {

    EMPTY(0, "非文件类型"),

    FILE(1, "普通文件"),

    COMPRESS(2, "压缩文件"),

    EXCEL(3, "EXCEL"),

    CSV(4, "CSV"),

    WORD(5, "WORD"),

    PDF(6, "PDF"),

    TEXT(7, "TXT"),

    PICTURE(8, "PICTURE"),

    AUDIO(9, "AUDIO"),

    VIDEO(10, "VIDEO"),

    PPT(11, "PPT"),

    BINARY(12, "BINARY");

    /**
     * 文件类型
     */
    private final int type;

    /**
     * 文件名称
     */
    private final String name;

}
