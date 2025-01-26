package com.neptune.cloud.drive.server.schedule.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neptune.cloud.drive.schedule.task.ScheduleTask;
import com.neptune.cloud.drive.server.common.event.RecordErrorLogEvent;
import com.neptune.cloud.drive.server.model.FileChunk;
import com.neptune.cloud.drive.server.service.IFileChunkService;
import com.neptune.cloud.drive.storage.engine.core.StorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CleanExpireChunkTask implements ScheduleTask, ApplicationContextAware {

    private final static int CLEAN_EXPIRE_CHUNK_COUNT = 1000;

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

    @Override
    public String getTaskName() {
        return "cleanExpireChunkTask";
    }

    @Override
    public void run() {
        // 1. 初始化滚动 ID
        long scrollChunkId = 1;
        // 2. 循环执行
        while (true) {
            // 3. 滚动查询文件分片
            List<FileChunk> expireChunks = scrollSelectExpireChunk(scrollChunkId);
            // 4. 判断是否存在过期的文件分片
            if (CollectionUtils.isEmpty(expireChunks)) {
                continue;
            }
            List<Long> expireChunkIds = expireChunks.stream()
                    .map(FileChunk::getChunkId).collect(Collectors.toList());
            // 5. 调用存储引擎删除文件分片
            cleanExpireChunk(expireChunks);
            // 6. 更新数据库中文件分片的状态
            removeExpireChunk(expireChunkIds);
            // 7. 更新滚动指针
            Optional<Long> optional = expireChunkIds.stream().max(Long::compareTo);

            if (optional.isPresent()) {
                scrollChunkId = optional.get();
            }
        }
    }

    /**
     * 查询过期的文件分片
     */
    private List<FileChunk> scrollSelectExpireChunk(long scrollChunkId) {
        QueryWrapper<FileChunk> wrapper = new QueryWrapper<FileChunk>()
                .ge("id", scrollChunkId)
                .le("expiration_time", new Date())
                .last(" limit " + CLEAN_EXPIRE_CHUNK_COUNT);
        return fileChunkService.list(wrapper) ;
    }

    /**
     * 物理删除文件分片
     */
    private void cleanExpireChunk(List<FileChunk> chunks) {
        try {
            storageEngine.deleteFile(new DeleteFileContext()
                    .setFilePaths(chunks.stream()
                            .map(FileChunk::getRealPath).collect(Collectors.toList())));
        } catch (IOException exception) {
            applicationContext.publishEvent(new RecordErrorLogEvent(0, "删除过期分片失败", this));
        }
    }

    private void removeExpireChunk(List<Long> chunkIds) {
        if (!fileChunkService.removeByIds(chunkIds)) {
            applicationContext.publishEvent(new RecordErrorLogEvent(0, "更新过期分片的状态失败", this));
        }
    }
}
