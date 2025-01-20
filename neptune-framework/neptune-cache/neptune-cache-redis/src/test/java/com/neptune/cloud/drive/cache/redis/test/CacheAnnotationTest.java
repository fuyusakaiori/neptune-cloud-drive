package com.neptune.cloud.drive.cache.redis.test;

import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheAnnotationTest {

    /**
     * sync = true: 表示缓存未命中时, 仅允许线程串行查询数据库, 防止缓存穿透
     */
    @Cacheable(cacheNames = CacheConstant.CLOUD_DRIVE_CACHE_NAME, key = "#name", sync = true)
    public String redisCache(String name) {
        StringBuilder value = new StringBuilder().
                append("hello").append(" ").append(name);
        log.info("RedisCacheTest redisCache: key: {}, value: {}", name, value);
        return value.toString();
    }

}
