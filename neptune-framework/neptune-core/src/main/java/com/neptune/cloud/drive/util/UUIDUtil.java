package com.neptune.cloud.drive.util;

import com.neptune.cloud.drive.constant.StringConstant;

import java.util.UUID;

/**
 * UUID 工具类
 */
public class UUIDUtil {

    public static String getUUID() {
        return UUID.randomUUID().toString().replace(StringConstant.HYPHEN, StringConstant.EMPTY).toUpperCase();
    }

}
