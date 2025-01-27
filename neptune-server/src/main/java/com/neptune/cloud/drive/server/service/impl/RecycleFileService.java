package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.common.enums.DirectoryEnum;
import com.neptune.cloud.drive.server.common.event.DeleteFileEvent;
import com.neptune.cloud.drive.server.common.event.DeleteUserFileEvent;
import com.neptune.cloud.drive.server.common.event.RestoreUserFileEvent;
import com.neptune.cloud.drive.server.context.file.ListUserFileContext;
import com.neptune.cloud.drive.server.context.recycle.DeleteRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.GetRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.RestoreRecycleFileContext;
import com.neptune.cloud.drive.server.context.user.GetUserChildFileContext;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IRecycleFileService;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecycleFileService implements IRecycleFileService, ApplicationContextAware {

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 回收站查询文件
     */
    @Override
    public List<UserFileVO> listRecycleFile(GetRecycleFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 调用接口查询用户被标记为删除的文件
        return userFileService.listUserFiles(new ListUserFileContext()
                .setUserId(context.getUserId()).setDelete(DeleteEnum.YES.getFlag()));
    }

    /**
     * 还原回收站文件
     */
    @Override
    public void restoreRecycleFile(RestoreRecycleFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询所有需要还原的文件
        List<UserFile> files = userFileService.listByIds(context.getFileIds());
        // 2. 判断是否查询成功
        if (CollectionUtils.isEmpty(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要还原的文件不存在");
        }
        // 2. 检查回收站文件是否可以还原
        checkRestoreRecycleFile(context.getUserId(), files);
        // 3. 还原回收站文件
        doRestoreRecycleFile(context.getUserId(), files);
        // 4. 还原回收站文件后置操作
        afterRestoreRecycleFile(context.getFileIds());
    }

    /**
     * 删除回收站文件: 前端主动触发? 不应该后端定时任务调用?
     */
    @Override
    public void deleteRecycleFile(DeleteRecycleFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询需要删除的文件
        List<UserFile> files = userFileService.listByIds(context.getFileIds());
        // 2. 判断是否查询成功
        if (CollectionUtils.isEmpty(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要删除的文件不存在");
        }
        // 3. 判断回收站的文件是否可以删除
        checkDeleteRecycleFile(context.getUserId(), files);
        // 4. 删除回收站的文件
        files = doDeleteRecycleFile(context.getUserId(), files);
        // 5. 异步调用存储引擎物理删除文件: 应该是需要删除的内容比较多, 采用异步完成
        afterDeleteRecycleFile(files);
    }

    //============================================= private =============================================

    /**
     * 检查是否可以还原文件
     */
    private void checkRestoreRecycleFile(long userId, List<UserFile> files) {
        // 1. 检查文件的状态是否为已删除
        if (files.stream()
                .anyMatch(file -> DeleteEnum.NO.getFlag() == file.getDelFlag())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "还原的文件中不在回收站中");
        }
        // 2. 检查文件是否都属于当前用户
        if (files.stream().anyMatch(file -> userId != file.getUserId())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "无权还原其他用户的文件");
        }
        // 3. 检查还原到相同目录下的文件是否存在相同的名字: 回收站可以同时存在相同名字的文件, 但是用户目录下不可以
        Set<String> fileNames = files.stream()
                .map(file -> file.getParentId() + StringConstant.COMMON_SEPARATOR + file.getFilename())
                .collect(Collectors.toSet());
        if (fileNames.size() != files.size()) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "还原到相同目录下存在拥有相同名字的文件, 请分开还原");
        }
        // 4. 检查还原的目标位置是否已经存在相同名字的文件或者目录
        for (UserFile file : files) {
            // TODO: 如果还原的文件或者目录数量很多, 那么这样判重是非常慢的
            QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                    .eq("userId", file.getUserId())
                    .eq("parent_id", file.getParentId())
                    .eq("filename", file.getFilename())
                    .eq("del_flag", DeleteEnum.NO.getFlag());
            if (userFileService.count(queryWrapper) > 0) {
                throw new BusinessException(ResponseCode.ERROR.getCode(), "还原文件的目录下已经存在相同名字的文件或者目录");
            }
        }
    }

    /**
     * 还原回收站文件
     */
    private void doRestoreRecycleFile(long userId, List<UserFile> files) {
        // 1. 设置需要还原的文件的状态
        for (UserFile file : files) {
            file.setDelFlag(DeleteEnum.NO.getFlag())
                    .setUpdateUser(userId).setUpdateTime(new Date());
        }
        // 2. 还原文件: 删除和还原的时候都没有递归更新子文件夹下的状态
        if (!userFileService.updateBatchById(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "还原文件失败");
        }
    }

    /**
     * 发布回收站文件还原的事件: 用于分享文件的还原
     */
    private void afterRestoreRecycleFile(List<Long> fileIds) {
        applicationContext.publishEvent(new RestoreUserFileEvent(fileIds, this));
    }

    /**
     * 检查是否可以物理删除文件
     */
    private void checkDeleteRecycleFile(long userId, List<UserFile> files) {
        // 1. 判断文件是否处于删除状态
        if (files.stream()
                .anyMatch(file -> file.getDelFlag() != DeleteEnum.YES.getFlag())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不允许清理未删除的文件");
        }
        // 2. 检查文件是否都属于当前用户
        if (files.stream().anyMatch(file -> userId != file.getUserId())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "无权删除其他用户的文件");
        }
    }

    /**
     * 删除回收站文件
     */
    private List<UserFile> doDeleteRecycleFile(long userId, List<UserFile> files) {
        // 1. 查询所有需要删除的文件: 因为被删除的文件中可能存在目录, 所以需要递归查询找到所有的文件
        List<UserFile> deleteFiles = userFileService.selectUserChildFiles(
                new GetUserChildFileContext().setUserId(userId).setFiles(files));
        // 2. 删除所有需要删除的文件
        if (userFileService.removeByIds(
                deleteFiles.stream().map(UserFile::getFileId).collect(Collectors.toList()))) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "删除回收站的文件失败");
        }
        return deleteFiles;
    }

    /**
     * 发布物理删除回收站文件的事件
     */
    private void afterDeleteRecycleFile(List<UserFile> files) {
        applicationContext.publishEvent(new DeleteFileEvent(files, this));
    }

}
