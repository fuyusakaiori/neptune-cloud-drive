package com.neptune.cloud.drive.server.common.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RecordErrorLogEvent extends ApplicationEvent {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 错误信息
     */
    private String message;


    public RecordErrorLogEvent(long userId, String message, Object source) {
        super(source);
        this.userId = userId;
        this.message = message;
    }
}
