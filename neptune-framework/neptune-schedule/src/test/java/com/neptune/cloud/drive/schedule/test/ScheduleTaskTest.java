package com.neptune.cloud.drive.schedule.test;

import com.neptune.cloud.drive.schedule.ScheduleManager;
import com.neptune.cloud.drive.schedule.test.config.ScheduleTestConfig;
import com.neptune.cloud.drive.schedule.test.task.SimpleScheduleTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScheduleTestConfig.class)
public class ScheduleTaskTest {

    @Autowired
    private ScheduleManager scheduleManager;

    @Autowired
    private SimpleScheduleTask simpleScheduleTask;

    @Test
    public void runScheduleTaskTest() throws InterruptedException {
        // 1. 表达式
        String cron = "0/5 * * * * ?";
        // 2. 启动定时任务
        String taskId = scheduleManager.startTask(simpleScheduleTask, cron);

        TimeUnit.SECONDS.sleep(10);

        // 3. 更新表达式
        cron = "0/1 * * * * ?";
        // 4. 更新定时任务
        scheduleManager.changeTask(taskId, cron);

        TimeUnit.SECONDS.sleep(10);

        // 5. 停止定时任务
        scheduleManager.stopTask(taskId);
    }

}
