package com.neptune.cloud.drive.server.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neptune.cloud.drive.server.common.enums.DirectoryEnum;
import com.neptune.cloud.drive.server.common.event.DeleteFileEvent;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IFileService;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.storage.engine.core.StorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.DeleteFileContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeleteFileEventListener implements ApplicationContextAware {

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @EventListener(DeleteFileEvent.class)
    public void deleteFile(DeleteFileEvent event) {
        // 1. 查询已经没有被用户引用的文件
        List<UserFile> unusedFiles = selectUnusedFiles(event.getDeleteFiles());
        // 2. 判断是否存在需要删除的文件
        if (CollectionUtils.isEmpty(unusedFiles)) {
            return;
        }
        // 3. 查询需要删除的文件的真实记录
        List<File> deleteFiles = fileService.listByIds(
                unusedFiles.stream().map(UserFile::getRealFileId).collect(Collectors.toList()));
        // 4. 判断是否查询成功
        if (CollectionUtils.isEmpty(deleteFiles)) {
            return;
        }
        // 5. 获取所有需要删除的文件的路径
        List<String> deleteFilePaths = deleteFiles.stream().map(File::getRealPath).collect(Collectors.toList());
        // 6. 调用存储引擎删除文件
        try {
            storageEngine.deleteFile(
                    new DeleteFileContext().setFilePaths(deleteFilePaths));
        } catch (IOException exception) {
            // TODO: 记录错误日志
        }
        // 7. 删除数据库中的记录
        if (!fileService.removeByIds(
                deleteFiles.stream().map(File::getFileId).collect(Collectors.toList()))) {
            // TODO 记录错误日志
        }
    }

    /**
     * 查询已经没有被用户引用的文件: 因为分享或者秒传都会使得同一份文件被多个用户记录引用
     */
    private List<UserFile> selectUnusedFiles(List<UserFile> deleteFiles) {
        return deleteFiles.stream()
                // 过滤目录
                .filter(deleteFile -> deleteFile.getFolderFlag() == DirectoryEnum.NO.getFlag())
                // 过滤还有引用的文件
                .filter(deleteFile -> userFileService.count(
                        new QueryWrapper<UserFile>().eq("real_file_id", deleteFile.getRealFileId())) == 0)
                .collect(Collectors.toList());
    }
}
