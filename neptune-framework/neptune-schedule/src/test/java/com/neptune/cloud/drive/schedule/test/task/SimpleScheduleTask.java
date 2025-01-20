package com.neptune.cloud.drive.schedule.test.task;

import com.neptune.cloud.drive.schedule.task.ScheduleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 测试定时任务
 */
@Slf4j
@Component
public class SimpleScheduleTask implements ScheduleTask {

    @Override
    public String getTaskName() {
        return "simple";
    }

    @Override
    public void run() {
        log.info("SimpleScheduleTask Hello");
    }
}
