package com.neptune.cloud.drive.server.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.config.CloudDriveSeverConfig;
import com.neptune.cloud.drive.server.context.file.UploadFileChunkContext;
import com.neptune.cloud.drive.server.mapper.FileChunkMapper;
import com.neptune.cloud.drive.server.model.FileChunk;
import com.neptune.cloud.drive.server.service.IFileChunkService;
import com.neptune.cloud.drive.storage.engine.core.StorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.StoreFileChunkContext;
import com.neptune.cloud.drive.util.IdUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

@Service
public class FileChunkService extends ServiceImpl<FileChunkMapper, FileChunk> implements IFileChunkService {

    @Autowired
    private CloudDriveSeverConfig config;

    @Autowired
    private StorageEngine storageEngine;

    /**
     * 分片上传文件: 后续会替换为分布式锁
     */
    @Override
    public synchronized boolean uploadFileChunk(UploadFileChunkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 调用存储引擎存储文件分片
        String chunkPath = storeFileChunk(
                context.getIdentifier(),
                context.getFileName(),
                context.getChunkSeq(),
                context.getChunkSize(),
                context.getChunk());
        // 2. 判断是否上传文件分片成功
        if (StringUtils.isEmpty(chunkPath)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "上传文件分片失败");
        }
        // 3. 记录存储的文件分片
        doUploadFileChunk(context.getIdentifier(), context.getChunkSeq(), context.getUserId(), chunkPath);
        // 4. 判断是否可以合并分片
        return checkFileChunkMerge(context.getIdentifier(), context.getUserId(), context.getChunkCount());
    }


    //============================================ private ============================================

    /**
     * 存储文件分片
     */
    private String storeFileChunk(String identifier, String fileName, long chunkSeq, long chunkSize, MultipartFile chunk) {
        try {
            // 1. 封装存储文件分片的上下文信息
            StoreFileChunkContext context = new StoreFileChunkContext()
                    .setIdentifier(identifier)
                    .setFileName(fileName)
                    .setChunkSeq(chunkSeq)
                    .setChunkSize(chunkSize)
                    .setChunk(chunk.getInputStream());
            // 2. 调用存储引擎存储文件分片
            return storageEngine.storeFileChunk(context);
        } catch (IOException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "上传文件分片失败");
        }
    }

    /**
     * 记录存储的文件分片
     */
    private void doUploadFileChunk(String identifier, long chunkSeq, long userId, String chunkPath) {
        // 1. 封装文件分片
        FileChunk fileChunk = assembleFileChunk(identifier, chunkSeq, userId, chunkPath);
        // 2. 存储文件
        if (!save(fileChunk)) {
            // 注: 如果保存分片记录失败, 那么不需要主动删除分片, 存在过期时间
            throw new BusinessException(ResponseCode.ERROR.getCode(), "存储文件分片失败");
        }
    }

    /**
     * 封装文件分片实体
     */
    private FileChunk assembleFileChunk(String identifier, long chunkSeq, long userId, String chunkPath) {
        return new FileChunk()
                .setChunkId(IdUtil.generate())
                .setIdentifier(identifier)
                .setChunkNumber(chunkSeq)
                .setRealPath(chunkPath)
                .setExpirationTime(DateUtil.offsetDay(new Date(), config.getChunkFileExpirationDay()))
                .setCreateUser(userId)
                .setCreateTime(new Date());
    }

    /**
     * 判断是否可以合并分片
     */
    private boolean checkFileChunkMerge(String identifier, long userId, long chunkCount) {
        // 1. 封装查询条件
        QueryWrapper<FileChunk> queryWrapper = new QueryWrapper<FileChunk>()
                .eq("identifier", identifier)
                .eq("create_user", userId);
        // 2. 查询分片的数量
        return count(queryWrapper) == chunkCount;
    }
}




