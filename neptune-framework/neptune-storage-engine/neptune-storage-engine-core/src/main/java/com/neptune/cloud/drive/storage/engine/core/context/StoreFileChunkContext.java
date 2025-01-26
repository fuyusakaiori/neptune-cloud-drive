package com.neptune.cloud.drive.storage.engine.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.InputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StoreFileChunkContext {

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
     * 分片编号
     */
    private long chunkSeq;

    /**
     * 分片大小
     */
    private long chunkSize;

    /**
     * 文件分片数量
     */
    private long chunkCount;

    /**
     * 分片
     */
    private InputStream chunk;
}
