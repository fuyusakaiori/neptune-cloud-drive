package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.share.CreateShareFileContext;
import com.neptune.cloud.drive.server.model.ShareFile;

public interface IShareFileService extends IService<ShareFile> {

    void createShareFile(CreateShareFileContext context);
}
