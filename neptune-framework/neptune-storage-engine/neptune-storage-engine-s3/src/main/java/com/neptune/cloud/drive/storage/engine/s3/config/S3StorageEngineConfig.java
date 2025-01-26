package com.neptune.cloud.drive.storage.engine.s3.config;

import com.aliyun.oss.OSSClient;
import com.neptune.cloud.drive.constant.BasicConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("com.neptune.cloud.drive.storage.engine.s3")
public class S3StorageEngineConfig {

    /**
     * 基础路径
     */
    private String basePath = BasicConstant.CLOUD_DRIVE;

    /**
     * 文件最大分片数量
     */
    private long maxChunkCount = 10000;

    /**
     * s3 请求域名
     */
    private String endpoint;

    /**
     * s3 桶
     */
    private String bucket;



    /**
     * s3 accessKey
     */
    private String accessKey;


    /**
     * s3 secretKey
     */
    private String secretKey;


    /**
     * s3 是否自动创建桶
     */
    private boolean autoCreateBucket = true;

    /**
     * 初始化 s3 客户端
     */
    @Bean
    public OSSClient ossClient() {
        return new OSSClient(endpoint, accessKey, secretKey);
    }

}
