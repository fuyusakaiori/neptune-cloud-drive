package com.neptune.cloud.drive.server;

import com.neptune.cloud.drive.constant.BasicConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(scanBasePackages = BasicConstant.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = BasicConstant.BASE_COMPONENT_SCAN_PATH)
public class CloudDriveBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(CloudDriveBootstrap.class);
    }

}
