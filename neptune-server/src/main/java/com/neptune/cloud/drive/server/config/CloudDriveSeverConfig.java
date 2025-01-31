package com.neptune.cloud.drive.server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("com.neptune.cloud.drive.server")
public class CloudDriveSeverConfig {

    @Value(value = "${server.port}")
    private int port;

    /**
     * 文件分片的过期天数
     */
    private int chunkFileExpirationDay = 1;

    /**
     * 文件分享链接前缀
     */
    private String shareLinkPrefix = "http://127.0.0.1:" + port + "/share/";

}
