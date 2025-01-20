package com.neptune.cloud.drive.schedule.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ScheduleTaskHolder implements Serializable {

    private ScheduleTask task;

    private ScheduledFuture<?> future;

}
