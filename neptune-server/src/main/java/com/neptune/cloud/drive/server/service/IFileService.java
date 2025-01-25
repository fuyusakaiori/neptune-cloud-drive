package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.file.DownloadFileContext;
import com.neptune.cloud.drive.server.context.file.GetFileContext;
import com.neptune.cloud.drive.server.context.file.MergeFileChunkContext;
import com.neptune.cloud.drive.server.context.file.UploadFileContext;
import com.neptune.cloud.drive.server.model.File;

import java.util.List;

public interface IFileService extends IService<File> {

    List<File> listFiles(GetFileContext context);

    void uploadFile(UploadFileContext context);

    void mergeFileChunk(MergeFileChunkContext context);

    void downloadFile(DownloadFileContext context);

    void previewFile(DownloadFileContext context);
}
