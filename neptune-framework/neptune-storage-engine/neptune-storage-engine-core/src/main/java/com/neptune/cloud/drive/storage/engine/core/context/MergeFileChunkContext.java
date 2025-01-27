package com.neptune.cloud.drive.storage.engine.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MergeFileChunkContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件唯一标识符
     */
    private String identifier;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件分片的路径
     */
    private List<String> chunkPaths;

}
