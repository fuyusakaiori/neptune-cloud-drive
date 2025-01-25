package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.event.RecordErrorLogEvent;
import com.neptune.cloud.drive.server.context.file.GetFileContext;
import com.neptune.cloud.drive.server.context.file.MergeFileChunkContext;
import com.neptune.cloud.drive.server.context.file.UploadFileContext;
import com.neptune.cloud.drive.server.mapper.FileMapper;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.model.FileChunk;
import com.neptune.cloud.drive.server.service.IFileChunkService;
import com.neptune.cloud.drive.server.service.IFileService;
import com.neptune.cloud.drive.storage.engine.core.StorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.DeleteFileContext;
import com.neptune.cloud.drive.storage.engine.core.context.StoreFileContext;
import com.neptune.cloud.drive.util.FileUtil;
import com.neptune.cloud.drive.util.IdUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService extends ServiceImpl<FileMapper, File> implements IFileService, ApplicationContextAware {

    @Autowired
    private IFileChunkService fileChunkService;

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 根据文件唯一标识符查询真实文件
     */
    @Override
    public List<File> listFiles(GetFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 封装查询条件
        QueryWrapper<File> queryWrapper = new QueryWrapper<File>()
                .eq("create_user", context.getUserId())
                .eq("identifier", context.getIdentifier());
        // 2. 根据唯一标识符查询
        return list(queryWrapper);
    }

    /**
     * 上传文件
     */
    @Override
    public void uploadFile(UploadFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 存储引擎上传文件
        String filaPath = storeFile(context.getFileName(), context.getFileSize(), context.getFile());
        // 2. 判断上传文件是否成功
        if (StringUtils.isEmpty(filaPath)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "上传文件失败");
        }
        // 2. 存储文件记录
        doUploadFile(context.getUserId(), context.getFileName(), context.getFileSize(), context.getIdentifier(), filaPath);
    }

    /**
     * 合并文件分片
     */
    @Override
    public void mergeFileChunk(MergeFileChunkContext context) {
        // 1. 调用文件分片接口合并分片
        String filePath = doMergeFileChunk(context.getUserId(), context.getIdentifier());
        // 2. 记录分片合并后的文件
        doUploadFile(
                context.getUserId(),
                context.getFileName(),
                context.getFileSize(),
                context.getIdentifier(),
                filePath);
    }


    //============================================== private ==============================================

    /**
     * 调用存储引擎存储文件
     */
    private String storeFile(String fileName, long fileSize, MultipartFile file) {
        try {
            // 1. 封装存储文件的上下文
            StoreFileContext context = new StoreFileContext()
                    .setFileName(fileName)
                    .setFileSize(fileSize)
                    .setFile(file.getInputStream());
            // 2. 调用存储引擎的方法存储文件
            return storageEngine.storeFile(context);
        } catch (IOException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "上传文件失败");
        }
    }

    /**
     * 存储文件记录
     */
    private void doUploadFile(long userId, String fileName, long fileSize, String identifier, String filepath) {
        // 1. 封装存储的文件
        File file = assembleFile(userId, fileName, fileSize, identifier, filepath);
        // 2. 记录存储的文件
        if (!save(file)) {
            try {
                // 3. 封装删除文件的上下文
                DeleteFileContext context = new DeleteFileContext()
                        .setFilePaths(Collections.singletonList(filepath));
                // 4. 调用存储引擎删除文件
                storageEngine.deleteFile(context);
            } catch (IOException exception) {
                afterUploadFileException(userId, filepath);
                throw new BusinessException(ResponseCode.ERROR.getCode(), "撤销上传失败的文件失败");
            }
        }
    }

    /**
     * 封装文件实体
     */
    private File assembleFile(long userId, String fileName, long fileSize, String identifier, String filepath) {
        return new File()
                .setFileId(IdUtil.generate())
                .setFilename(fileName)
                .setFileSuffix(FileUtil.getFileSuffix(fileName))
                .setFileSize(String.valueOf(fileSize))
                .setFileSizeDesc(FileUtil.fileSize2DisplaySize(fileSize))
                .setRealPath(filepath)
                .setCreateUser(userId)
                .setCreateTime(new Date());
    }

    /**
     * 存储文件记录失败并且删除已经删除的文件出现异常时, 后置操作
     */
    public void afterUploadFileException(long userId, String filePath) {
        applicationContext.publishEvent(
                new RecordErrorLogEvent(userId, String.format("撤销已上传的文件失败, %s", filePath), this));
    }

    /**
     * 合并文件分片
     */
    private String doMergeFileChunk(long userId, String identifier) {
        // 1. 封装查询文件分片的条件
        QueryWrapper<FileChunk> queryWrapper = new QueryWrapper<FileChunk>()
                .eq("create_user", userId)
                .eq("identifier", identifier)
                .gt("expiration_time", new Date());
        // 2. 查询文件分片
        List<FileChunk> chunks = fileChunkService.list(queryWrapper);
        // 3. 判断文件是否存在对应的分片
        if (CollectionUtils.isEmpty(chunks)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件不存在对应的分片");
        }
        // 4. 获取所有分片对应的存储路径
        List<String> chunkPaths = chunks.stream()
                .sorted(Comparator.comparing(FileChunk::getChunkNumber))
                .map(FileChunk::getRealPath).collect(Collectors.toList());
        // 5. 调用存储引擎合并文件分片
        String filePath = StringConstant.EMPTY;
        try {
            filePath = storageEngine.mergeFileChunk(new com.neptune.cloud.drive.storage.engine.core.context.MergeFileChunkContext()
                    .setIdentifier(identifier)
                    .setChunkPaths(chunkPaths));
        } catch (IOException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "合并文件分片失败");
        }
        // 6. 移除已经合并的文件分片记录
        List<Long> chunkIds = chunks.stream()
                .sorted(Comparator.comparing(FileChunk::getChunkNumber))
                .map(FileChunk::getChunkId)
                .collect(Collectors.toList());
        if (fileChunkService.removeByIds(chunkIds)) {
            // TODO 记录异常信息
        }
        return filePath;
    }
}
