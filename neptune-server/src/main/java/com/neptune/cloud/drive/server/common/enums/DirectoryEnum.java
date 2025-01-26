package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 是否为目录
 */
@Getter
@AllArgsConstructor
public enum DirectoryEnum {

    NO(0),

    YES(1);

    private final int flag;

    public static DirectoryEnum getDirectoryEnum(int flag) {
        return Arrays.stream(DirectoryEnum.values())
                .filter(directory -> directory.flag == flag)
                .findFirst().orElse(NO);
    }

}
