package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.context.share.CreateShareFileContext;
import com.neptune.cloud.drive.server.mapper.ShareFileMapper;
import com.neptune.cloud.drive.server.model.ShareFile;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IShareFileService;
import com.neptune.cloud.drive.server.service.IUserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShareFileService extends ServiceImpl<ShareFileMapper, ShareFile> implements IShareFileService {

    @Override
    public void createShareFile(CreateShareFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 记录分享的文件
        doCreateShareFile(context.getUserId(), context.getShareId(), context.getShareFileIds());
    }

    /**
     * 记录分享的文件
     */
    private void doCreateShareFile(long userId, long shareId, List<Long> shareFileIds) {
        // 1. 将分享的文件封装成实体
        List<ShareFile> shareFiles = shareFileIds.stream()
                .map(shareFileId -> assembleShareFile(userId, shareId, shareFileId))
                .collect(Collectors.toList());
        // 2. 及记录分享的文件
        if (!saveBatch(shareFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享文件失败");
        }
    }

    private ShareFile assembleShareFile(long userId, long shareId, long shareFileId) {
        return new ShareFile()
                .setShareId(shareId)
                .setFileId(shareFileId)
                .setCreateUser(userId)
                .setCreateTime(new Date());
    }
}




