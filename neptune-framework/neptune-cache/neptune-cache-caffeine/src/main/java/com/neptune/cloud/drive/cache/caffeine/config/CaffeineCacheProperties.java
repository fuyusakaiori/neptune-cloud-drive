package com.neptune.cloud.drive.cache.caffeine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "com.neptune.cloud.drive.cache.caffeine")
public class CaffeineCacheProperties {

    /**
     * 本地缓存初始化容量
     */
    private int initCacheCapacity = 256;

    /**
     * 本地缓存最大容量, 超过最大容量会按照最近最少使用淘汰
     */
    private long maxCacheCapacity = 10000L;

    /**
     * 是否允许使用空值作为缓存的 value
     */
    private boolean allowNullValue = true;

}
