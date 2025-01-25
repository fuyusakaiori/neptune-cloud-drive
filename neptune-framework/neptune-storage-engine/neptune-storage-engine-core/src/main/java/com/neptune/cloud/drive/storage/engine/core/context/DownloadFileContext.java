package com.neptune.cloud.drive.storage.engine.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.OutputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadFileContext {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 下载的文件
     */
    private OutputStream file;

}
