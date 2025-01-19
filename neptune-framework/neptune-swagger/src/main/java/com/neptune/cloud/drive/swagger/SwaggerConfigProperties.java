package com.neptune.cloud.drive.swagger;

import com.neptune.cloud.drive.constant.BasicConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Swagger 配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger2")
public class SwaggerConfigProperties {

    private boolean show = true;

    private String groupName = BasicConstant.CLOUD_DRIVE;

    private String basePackage = BasicConstant.BASE_COMPONENT_SCAN_PATH;

    private String title = BasicConstant.CLOUD_DRIVE;

    private String description = BasicConstant.CLOUD_DRIVE;

    private String termsOfServiceUrl = "http://127.0.0.1:${server.port}";

    private String contactName = "fuyusakaiori";

    private String contactUrl = "https://space.bilibili.com/6074988";

    private String contactEmail = "670232228@qq.com";

    private String version = "1.0";

}
