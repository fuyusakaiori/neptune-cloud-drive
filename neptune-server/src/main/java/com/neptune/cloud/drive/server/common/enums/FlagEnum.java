package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否删除
 */
@Getter
@AllArgsConstructor
public enum FlagEnum {

    NO(0),

    YES(1);

    private final int flag;

}
