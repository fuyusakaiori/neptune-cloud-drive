package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否为目录
 */
@Getter
@AllArgsConstructor
public enum DirectoryEnum {

    NO(0),

    YES(1);

    private final int flag;

}
