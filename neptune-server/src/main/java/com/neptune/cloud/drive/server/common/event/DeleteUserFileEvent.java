package com.neptune.cloud.drive.server.common.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class DeleteUserFileEvent extends ApplicationEvent {

    private final List<Long> fileIdList;

    public DeleteUserFileEvent(Object source, List<Long> fileIdList) {
        super(source);
        this.fileIdList = fileIdList;
    }
}
