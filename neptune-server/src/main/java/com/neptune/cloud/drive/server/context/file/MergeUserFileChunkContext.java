package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MergeUserFileChunkContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 目录 ID
     */
    private long parentId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件唯一标识符
     */
    private String identifier;

    /**
     * 文件大小
     */
    private long fileSize;
}
