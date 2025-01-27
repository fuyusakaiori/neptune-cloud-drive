package com.neptune.cloud.drive.server.common.event;

import com.neptune.cloud.drive.server.model.UserFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class DeleteFileEvent extends ApplicationEvent {

    /**
     * 需要删除的文件和目录
     */
    private final List<UserFile> deleteFiles;

    public DeleteFileEvent(List<UserFile> deleteFiles, Object source) {
        super(source);
        this.deleteFiles = deleteFiles;
    }

}
