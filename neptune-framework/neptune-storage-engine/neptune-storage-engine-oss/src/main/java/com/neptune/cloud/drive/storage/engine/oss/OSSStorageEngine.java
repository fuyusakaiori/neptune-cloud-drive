package com.neptune.cloud.drive.storage.engine.oss;

import com.neptune.cloud.drive.storage.engine.core.AbstractStorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.DeleteFileContext;
import com.neptune.cloud.drive.storage.engine.core.context.MergeFileChunkContext;
import com.neptune.cloud.drive.storage.engine.core.context.StoreFileChunkContext;
import com.neptune.cloud.drive.storage.engine.core.context.StoreFileContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OSSStorageEngine extends AbstractStorageEngine {


    @Override
    protected String doStoreFile(StoreFileContext context) throws IOException {
        return "";
    }

    @Override
    protected void doDeleteFile(DeleteFileContext context) throws IOException {

    }

    @Override
    protected String doStoreFileChunk(StoreFileChunkContext context) throws IOException {
        return "";
    }

    @Override
    protected String doMergeFileChunk(MergeFileChunkContext context) throws IOException {
        return "";
    }
}
