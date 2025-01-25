package com.neptune.cloud.drive.storage.engine.core;


import cn.hutool.core.lang.Assert;
import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.storage.engine.core.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.IOException;

public abstract class AbstractStorageEngine implements StorageEngine {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 获取缓存
     */
    protected Cache getCache() {
        return cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
    }

    @Override
    public String storeFile(StoreFileContext context) throws IOException {
        // 1. 校验存储文件的参数是否合法
        checkStoreFileContext(context);
        // 2. 调用存储引擎的具体实现
        return doStoreFile(context);
    }

    @Override
    public String storeFileChunk(StoreFileChunkContext context) throws IOException {
        // 1. 校验存储文件分片的参数是否合法
        checkStoreFileChunkContext(context);
        // 2. 调用存储引擎的具体是心啊
        return doStoreFileChunk(context);
    }

    @Override
    public void deleteFile(DeleteFileContext context) throws IOException {
        // 1. 校验删除文件的参数是否合法
        checkDeleteFileContext(context);
        // 2. 调用存储引擎的具体实现
        doDeleteFile(context);
    }

    @Override
    public String mergeFileChunk(MergeFileChunkContext context) throws IOException {
        // 1. 校验合并文件分片的参数是否合法
        checkMergeFileChunkContext(context);
        // 2. 调用存储引擎的具体实现
        return doMergeFileChunk(context);
    }

    @Override
    public void downloadFile(DownloadFileContext context) throws IOException {
        // 1. 校验下载文件的参数是否合法
        checkDownloadFileContext(context);
        // 2. 调用存储引擎的具体实现
        doDownloadFile(context);
    }

    /**
     * 校验存储文件的参数
     */
    private void checkStoreFileContext(StoreFileContext context) {
        Assert.notNull(context, "上下文不可以为空");
        Assert.notNull(context.getFileName(), "文件名称不可以为空");
        Assert.notNull(context.getFileSize(), "文件大小不可以为空");
        Assert.notNull(context.getFile(), "文件不可以为空");
    }

    /**
     * 校验删除文件的参数
     */
    private void checkDeleteFileContext(DeleteFileContext context) {
        Assert.notNull(context, "上下文不可以为空");
        Assert.notEmpty(context.getFilePaths(), "删除的文件路径不能为空");
    }

    /**
     * 校验存储文件分片的参数
     */
    private void checkStoreFileChunkContext(StoreFileChunkContext context) {
        Assert.notNull(context, "上下文不可以为空");
        Assert.notNull(context.getFileName(), "文件名称不可以为空");
        Assert.notNull(context.getIdentifier(), "文件唯一标识符不可以为空");
        Assert.notNull(context.getChunkSeq(), "文件分片编号不可以为空");
        Assert.notNull(context.getChunkSize(), "文件大小不可以为空");
        Assert.notNull(context.getChunk(), "文件分片不可以为空");
    }

    /**
     * 校验合并文件分片的参数
     */
    private void checkMergeFileChunkContext(MergeFileChunkContext context) {
        Assert.notNull(context, "上下文不可以为空");
        Assert.notNull(context.getFileName(), "文件名称不可以为空");
        Assert.notNull(context.getIdentifier(), "文件唯一标识符不可以为空");
        Assert.notEmpty(context.getChunkPaths(), "文件分片路径不可以为空");
    }

    /**
     * 校验下载文件的参数
     */
    private void checkDownloadFileContext(DownloadFileContext context) {
        Assert.notNull(context, "上下文不可以为空");
        Assert.notNull(context.getFilePath(), "文件路径不可以为空");
    }

    protected abstract String doStoreFile(StoreFileContext context) throws IOException;

    protected abstract void doDeleteFile(DeleteFileContext context) throws IOException;

    protected abstract String doStoreFileChunk(StoreFileChunkContext context) throws IOException;

    protected abstract String doMergeFileChunk(MergeFileChunkContext context) throws IOException;

    protected abstract void doDownloadFile(DownloadFileContext context) throws IOException;
}
