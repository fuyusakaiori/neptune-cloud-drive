package com.neptune.cloud.drive.cache.redis.test;

import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootApplication
@SpringBootTest(classes = CacheAnnotationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheAnnotationTest cacheAnnotationTest;

    /**
     * 方法调用使用缓存
     */
    @Test
    public void cacheManagerTest() {
        // 1. 获取 cache
        Cache cache = cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        // 2. 断言 cache 存在
        Assert.assertNotNull(cache);
        // 3. 放入 kv
        cache.put("name", "fuyusakaiori");
        // 4. 判断是否放入缓存
        Assert.assertEquals("fuyusakaiori", cache.get("name", String.class));
    }

    /**
     * 注解使用缓存
     */
    @Test
    public void caffeineCacheTest() {
        for (int index = 0; index < 2; index++) {
            cacheAnnotationTest.redisCache("fuyusakaiori");
        }
    }

}
