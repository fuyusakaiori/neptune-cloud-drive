package com.neptune.cloud.drive.schedule.test.config;

import com.neptune.cloud.drive.constant.BasicConstant;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 定时任务配置类: 测试类不要使用相同的名字
 */
@SpringBootConfiguration
@ComponentScan(value = BasicConstant.BASE_COMPONENT_SCAN_PATH + ".schedule")
public class ScheduleTestConfig {

}
