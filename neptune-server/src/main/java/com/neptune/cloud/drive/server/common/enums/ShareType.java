package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享类型
 */
@Getter
@AllArgsConstructor
public enum ShareType {


    NEED_SHARE_CODE(0, "有提取码");

    private final int code;

    private final String description;

}
