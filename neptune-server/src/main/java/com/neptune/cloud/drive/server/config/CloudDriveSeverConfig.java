package com.neptune.cloud.drive.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("com.neptune.cloud.drive.server")
public class CloudDriveSeverConfig {

    /**
     * 文件分片的过期天数
     */
    private Integer chunkFileExpirationDay = 1;

}
