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
public class StoreFileContext {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件字节流
     */
    private InputStream file;
}
