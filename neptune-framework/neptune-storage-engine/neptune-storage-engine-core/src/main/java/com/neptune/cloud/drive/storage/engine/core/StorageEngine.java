package com.neptune.cloud.drive.storage.engine.core;

import com.neptune.cloud.drive.storage.engine.core.context.*;

import java.io.IOException;
import java.io.OutputStream;

public interface StorageEngine {

    /**
     * 存储文件
     */
    String storeFile(StoreFileContext context) throws IOException;

    /**
     * 存储文件分片
     */
    String storeFileChunk(StoreFileChunkContext context) throws IOException;

    /**
     * 删除文件
     */
    void deleteFile(DeleteFileContext context) throws IOException;

    /**
     * 合并文件分片
     */
    String mergeFileChunk(MergeFileChunkContext context) throws IOException;


    void downloadFile(DownloadFileContext context) throws IOException;
}
