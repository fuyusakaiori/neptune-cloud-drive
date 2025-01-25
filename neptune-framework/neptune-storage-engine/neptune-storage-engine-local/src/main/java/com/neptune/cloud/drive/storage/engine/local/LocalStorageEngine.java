package com.neptune.cloud.drive.storage.engine.local;

import cn.hutool.core.date.DateUtil;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.storage.engine.core.AbstractStorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.*;
import com.neptune.cloud.drive.storage.engine.local.config.LocalStorageEngineConfig;
import com.neptune.cloud.drive.util.FileUtil;
import com.neptune.cloud.drive.util.UUIDUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 本地磁盘存储引擎
 */
@Component
public class LocalStorageEngine extends AbstractStorageEngine {

    /**
     * 配置类
     */
    @Autowired
    private LocalStorageEngineConfig config;


    /**
     * 存储文件
     */
    @Override
    protected String doStoreFile(StoreFileContext context) throws IOException {
        // 1. 获取基础路径
        String baseFilePath = config.getBaseFilePath();
        // 2. 获取文件名称
        String filePath = generateStoreFilePath(baseFilePath, context.getFileName());
        // 3. 调用 sendfile 持久化存储文件
        writeInputStream2File(context.getFile(), new File(filePath), context.getFileSize());

        return filePath;
    }

    /**
     * 删除文件
     */
    @Override
    protected void doDeleteFile(DeleteFileContext context) throws IOException {
        // 1. 判断需要删除的文件是否为空
        if (CollectionUtils.isEmpty(context.getFilePaths())) {
            return;
        }
        // 2. 循环删除文件
        for (String filePath : context.getFilePaths()) {
            FileUtils.forceDelete(new File(filePath));
        }
    }

    /**
     * 存储文件分片
     */
    @Override
    protected String doStoreFileChunk(StoreFileChunkContext context) throws IOException {
        // 1. 获取分片基础路径
        String baseFileChunkPath = config.getBaseFileChunkPath();
        // 2. 获取文件名称
        String filePath = generateStoreFileChunkPath(baseFileChunkPath, context.getIdentifier(), context.getChunkSeq());
        // 3. 调用 sendfile 持久化存储文件
        writeInputStream2File(context.getChunk(), new File(filePath), context.getChunkSize());

        return filePath;
    }

    /**
     * 合并文件分片
     */
    @Override
    protected String doMergeFileChunk(MergeFileChunkContext context) throws IOException {
        // 1. 获取文件的基础路径
        String baseFilePath = config.getBaseFilePath();
        // 2. 生成合并分片后的文件路径
        String filePath = generateStoreFilePath(baseFilePath, context.getFileName());
        // 3. 创建合并后的文件
        if (createFile(new File(filePath))) {
            throw new IOException("创建合并分片后的文件失败");
        }
        // 4. 遍历所有分片并写入文件中
        for (String chunkPath : context.getChunkPaths()) {
            // 将文件分片追加写入文件中
            Files.write(Paths.get(filePath), Files.readAllBytes(Paths.get(chunkPath)), StandardOpenOption.APPEND);
        }
        // 5. 删除所有的文件分片
        for (String chunkPath : context.getChunkPaths()) {
            FileUtils.forceDelete(new File(chunkPath));
        }
        return filePath;
    }

    /**
     * 下载文件
     */
    @Override
    protected void doDownloadFile(DownloadFileContext context) throws IOException {
        // 1. 获取需要读取的文件的句柄
        File file = new File(context.getFilePath());
        // 2. 获取需要读取的文件的 channel
        FileChannel inputChannel = new FileInputStream(file).getChannel();
        // 3. 获取需要写入的文件的 channel
        WritableByteChannel outputChannel = Channels.newChannel(context.getFile());
        // 4. 零拷贝 (sendfile) 从输入 channel 到输出 channel
        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
        // 5. 刷新
        context.getFile().flush();
        // 6. 关闭流
        outputChannel.close();
        inputChannel.close();
    }

    /**
     * 生成文件的存储路径: 基础路径 + 年 + 月 + 日 + 随机的文件名称
     */
    private String generateStoreFilePath(String basePath, String fileName) {
        return basePath +
                File.separator +
                DateUtil.thisYear() +
                File.separator +
                (DateUtil.thisMonth() + 1) +
                File.separator +
                DateUtil.thisDayOfMonth() +
                File.separator +
                UUIDUtil.getUUID() +
                FileUtil.getFileSuffix(fileName);
    }

    /**
     * <p>生成文件分片的存储路径</p>
     * <p>生成规则：基础路径 + 年 + 月 + 日 + 唯一标识 + 随机的分片名称 + __,__ + 文件分片的下标</p>
     */
    private String generateStoreFileChunkPath(String basePath, String identifier, long chunkSeq) {
        return basePath +
                File.separator +
                DateUtil.thisYear() +
                File.separator +
                (DateUtil.thisMonth() + 1) +
                File.separator +
                DateUtil.thisDayOfMonth() +
                File.separator +
                identifier +
                File.separator +
                UUIDUtil.getUUID() +
                StringConstant.COMMON_SEPARATOR +
                chunkSeq;
    }

    /**
     * 将输入流的内容写入到另一个文件
     */
    private void writeInputStream2File(InputStream inputStream, File targetFile, Long totalSize) throws IOException {
        // 1. 创建文件
        if (!createFile(targetFile)) {
            throw new IOException("创建存储文件的目录失败");
        }
        // 2. 初始化随机读写文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile, "rw");
        // 3. 初始化目标文件 channel
        FileChannel outputChannel = randomAccessFile.getChannel();
        // 4. 初始化读取文件 channel
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        // 5. 零拷贝 (sendfile) 到另一个文件
        outputChannel.transferFrom(inputChannel, 0L, totalSize);
        // 6. 关闭文件流
        inputChannel.close();
        outputChannel.close();
        randomAccessFile.close();
        inputStream.close();
    }

    /**
     * 创建文件
     */
    public boolean createFile(File targetFile) throws IOException {
        // 1. 创建父目录
        if (!targetFile.getParentFile().exists()) {
            return targetFile.getParentFile().mkdirs();
        }
        // 2. 创建文件
        return targetFile.createNewFile();
    }
}
