package com.neptune.cloud.drive.storage.engine.local.config;

import com.neptune.cloud.drive.constant.BasicConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@Configuration
@ConfigurationProperties("com.neptune.cloud.drive.storage.engine.local")
public class LocalStorageEngineConfig {

    /**
     * 文件基础路径
     */
    private String baseFilePath = System.getProperty("user.home") + File.separator + BasicConstant.CLOUD_DRIVE;

    /**
     * 文件分片基础路径
     */
    private String baseFileChunkPath = System.getProperty("user.home") + File.separator + BasicConstant.CLOUD_DRIVE + File.separator + "chunks";;

}
