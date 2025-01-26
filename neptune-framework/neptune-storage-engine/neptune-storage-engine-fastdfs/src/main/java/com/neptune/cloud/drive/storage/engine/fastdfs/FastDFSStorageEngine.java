package com.neptune.cloud.drive.storage.engine.fastdfs;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.storage.engine.core.AbstractStorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.*;
import com.neptune.cloud.drive.storage.engine.fastdfs.config.FastDFSStorageEngineConfig;
import com.neptune.cloud.drive.util.FileUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class FastDFSStorageEngine extends AbstractStorageEngine {

    /**
     * fastdfs 客户端
     */
    @Autowired
    private FastFileStorageClient client;

    /**
     * fastdfs 配置
     */
    @Autowired
    private FastDFSStorageEngineConfig config;

    /**
     * 上传文件: 是否需要根据用户划分 group
     */
    @Override
    protected String doStoreFile(StoreFileContext context) throws IOException {
        // 1. 直接调用接口上传文件
        StorePath storePath = client.uploadFile(config.getGroup(),
                context.getFile(), context.getFileSize(), FileUtil.getFileExtName(context.getFileName()));
        // 2. 判断是否上传成功
        if (Objects.isNull(storePath)) {
            throw new IOException("上传文件失败");
        }
        // 3. 全路径: group/path
        return storePath.getFullPath();
    }

    /**
     * 删除文件
     */
    @Override
    protected void doDeleteFile(DeleteFileContext context) throws IOException {
        // 1. 遍历所有需要删除的文件
        for (String filePath : context.getFilePaths()) {
            // 2. 删除文件
            client.deleteFile(filePath);
        }
    }

    /**
     * 上传文件分片
     */
    @Override
    protected String doStoreFileChunk(StoreFileChunkContext context) throws IOException {
        throw new IOException("不支持文件分片上传");
    }

    /**
     * 合并文件分片
     */
    @Override
    protected String doMergeFileChunk(MergeFileChunkContext context) throws IOException {
        throw new IOException("不支持合并文件分片");
    }

    /**
     * 下载文件
     */
    @Override
    protected void doDownloadFile(DownloadFileContext context) throws IOException {
        // 1. 获取 group 和 path 切割点
        int position = context.getFilePath().indexOf(StringConstant.SLASH);
        // 2. 判断是否切割成功
        if (position < 0) {
            throw new IOException("下载文件失败");
        }
        // 3. 获取文件对应的 group
        String group = context.getFilePath().substring(0, position);
        // 4. 获取对应的文件路径
        String filePath = context.getFilePath().substring(position);
        // 5. 下载文件
        byte[] downloadBytes = client.downloadFile(group, filePath, new DownloadByteArray());
        // 6. 判断是否下载成功
        if (ArrayUtils.isEmpty(downloadBytes)) {
            throw new IOException("下载文件失败");
        }
        // 7. 将下载的内容写入输出流中
        context.getFile().write(downloadBytes);
        context.getFile().flush();
        context.getFile().close();
    }
}
