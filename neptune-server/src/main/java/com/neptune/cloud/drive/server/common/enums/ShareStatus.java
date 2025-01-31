package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享链接的状态
 */
@Getter
@AllArgsConstructor
public enum ShareStatus {

    /**
     * 正常状态
     */
    NORMAL(0, "正常状态"),

    /**
     * 被删除的状态
     */
    DELETED(1, "被删除");

    private final int status;

    private final String description;


}
