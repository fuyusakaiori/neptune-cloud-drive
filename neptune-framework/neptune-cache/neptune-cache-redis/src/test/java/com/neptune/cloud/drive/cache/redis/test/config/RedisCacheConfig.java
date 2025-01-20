package com.neptune.cloud.drive.cache.redis.test.config;

import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.constant.BasicConstant;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@SpringBootConfiguration
@EnableCaching
@ComponentScan(value = BasicConstant.BASE_COMPONENT_SCAN_PATH + ".cache.redis.test")
public class RedisCacheConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 1. 初始化序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 2. 初始化客户端
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 3. 设置连接工厂
        redisTemplate.setConnectionFactory(factory);
        // 4. 设置 key 的序列化器
        redisTemplate.setKeySerializer(stringSerializer);
        // 5. 设置 value 的序列化器
        redisTemplate.setValueSerializer(jacksonSerializer);
        // 6. 设置 hash key 的序列化器
        redisTemplate.setHashKeySerializer(stringSerializer);
        // 7. 设置 hash value 的序列化器
        redisTemplate.setHashValueSerializer(jacksonSerializer);
        return redisTemplate;
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 1. 初始化 configuration
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class)));
        // 2. 初始化 cache manager
        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(factory))
                // 指定缓存配置
                .cacheDefaults(configuration)
                // 指定开启缓存事务
                .transactionAware()
                .build();
    }

}
