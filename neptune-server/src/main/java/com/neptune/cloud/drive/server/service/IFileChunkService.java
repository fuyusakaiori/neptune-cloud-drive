package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.file.UploadFileChunkContext;
import com.neptune.cloud.drive.server.model.FileChunk;

public interface IFileChunkService extends IService<FileChunk> {

    boolean uploadFileChunk(UploadFileChunkContext context);
}
