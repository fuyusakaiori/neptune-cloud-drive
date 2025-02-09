package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CopyUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 移动的文件 ID
     */
    private List<Long> sourceIds;

    /**
     * 目标 ID
     */
    private long targetId;

    /**
     * 是否为分享
     */
    private boolean share;

}
