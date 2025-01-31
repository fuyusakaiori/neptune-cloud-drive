package com.neptune.cloud.drive.server.common.enums;

import com.neptune.cloud.drive.constant.BasicConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 分享链接的过期时间类型
 */
@Getter
@AllArgsConstructor
public enum ShareDayType {

    /**
     * 永不过期
     */
    PERMANENT_VALIDITY(0, 0, "永久有效"),

    /**
     * 7 天过期
     */
    SEVEN_DAYS_VALIDITY(1, 7, "七天有效"),

    /**
     * 30 天过期
     */
    THIRTY_DAYS_VALIDITY(2, 30, "三十天有效");


    private final int type;

    private final int expire;

    private final String description;

    public static int getExpireDay(int type) {
        for (ShareDayType value : ShareDayType.values()) {
            if (value.getType() == type) {
                return value.getExpire();
            }
        }
        return BasicConstant.NEGATIVE_ONE;
    }
}
