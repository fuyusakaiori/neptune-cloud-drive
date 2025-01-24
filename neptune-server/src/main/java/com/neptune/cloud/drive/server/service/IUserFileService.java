package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.context.user.GetUserRootDirContext;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.vo.UserFileVO;

import java.util.List;

public interface IUserFileService extends IService<UserFile> {

    long createUserDirectory(CreateUserDirectoryContext context);

    UserFile selectUserRootDir(GetUserRootDirContext context);

    void deleteUserFile(DeleteUserFileContext context);

    List<UserFileVO> listUserFiles(ListUserFileContext context);

    void renameUserFile(RenameUserFileContext context);

    boolean secondUploadUserFile(SecondUploadUserFileContext context);
}
