package com.neptune.cloud.drive.schedule.task;

/**
 * 定时任务接口
 */
public interface ScheduleTask extends Runnable {

    /**
     * 获取定时任务名称
     */
    String getTaskName();

}
