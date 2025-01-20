package com.neptune.cloud.drive.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableCaching
public class CaffeineCacheConfig {

    @Autowired
    private CaffeineCacheProperties properties;

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        caffeineCacheManager.setAllowNullValues(properties.isAllowNullValue());
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().
                initialCapacity(properties.getInitCacheCapacity()).
                maximumSize(properties.getMaxCacheCapacity());
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

}
