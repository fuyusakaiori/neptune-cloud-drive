package com.neptune.cloud.drive.server.common.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class RestoreUserFileEvent extends ApplicationEvent {

    private final List<Long> fileIds;

    public RestoreUserFileEvent(List<Long> fileIds, Object source) {
        super(source);
        this.fileIds = fileIds;
    }
}
