package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.CreateUserFolderContext;
import com.neptune.cloud.drive.server.model.UserFile;

public interface IUserFileService extends IService<UserFile> {

    long createUserFolder(CreateUserFolderContext context);

}
