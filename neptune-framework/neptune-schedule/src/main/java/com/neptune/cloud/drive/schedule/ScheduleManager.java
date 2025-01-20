package com.neptune.cloud.drive.schedule;

import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.schedule.task.ScheduleTask;
import com.neptune.cloud.drive.schedule.task.ScheduleTaskHolder;
import com.neptune.cloud.drive.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务管理器
 */
@Slf4j
@Component
public class ScheduleManager {

    /**
     * 定时任务缓存
     */
    private final Map<String, ScheduleTaskHolder> taskCache = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 启动定时任务
     */
    public String startTask(ScheduleTask task, String cron) {
        // 0. 判断定时任务和表达式是否合法
        if (Objects.isNull(task) || StringUtils.isEmpty(cron)) {
            return StringConstant.EMPTY;
        }
        // 1. 生成任务标识
        String taskId = UUIDUtil.getUUID();
        // 2. 执行定时任务
        ScheduledFuture<?> taskFuture = taskScheduler.schedule(task, new CronTrigger(cron));
        // 3. 封装定时任务和定时任务结果
        ScheduleTaskHolder taskHolder = new ScheduleTaskHolder(task, taskFuture);
        // 4. 放入缓存中, 从而支持定时任务动态编排
        taskCache.put(taskId, taskHolder);
        return taskId;
    }

    /**
     * 停止定时任务
     */
    public boolean stopTask(String taskId) {
        // 0. 判断任务标识是否为空
        if (StringUtils.isEmpty(taskId)) {
            return false;
        }
        // 1. 获取定时任务
        ScheduleTaskHolder taskHolder = taskCache.get(taskId);
        // 2. 判断定时任务是否为空
        if (Objects.isNull(taskHolder)) {
            return false;
        }
        // 3. 获取定时任务的执行结果
        ScheduledFuture<?> taskFuture = taskHolder.getFuture();
        // 4. 判断定时任务执行结果是否为空
        if (Objects.isNull(taskFuture)) {
            return false;
        }
        // 5. 等待认为执行结束后再停止任务
        if (!taskFuture.cancel(false)) {
            return false;
        }
        // 6. 移除定时任务缓存
        taskCache.remove(taskId);
        return true;
    }

    /**
     * 更新定时任务
     */
    public String changeTask(String taskId, String cron) {
        // 0. 判断任务标识和表达式是否为空
        if (StringUtils.isAnyEmpty(taskId, cron)) {
            return StringConstant.EMPTY;
        }
        // 1. 获取定时任务
        ScheduleTaskHolder taskHolder = taskCache.get(taskId);
        // 2. 判断是否为空
        if (Objects.isNull(taskHolder)) {
            return StringConstant.EMPTY;
        }
        // 3. 等待定时任务执行结束后, 停止定时任务
        if (!stopTask(taskId)) {
            return StringConstant.EMPTY;
        }
        // 4. 重新启动定时任务
        return startTask(taskHolder.getTask(), cron);
    }

}
