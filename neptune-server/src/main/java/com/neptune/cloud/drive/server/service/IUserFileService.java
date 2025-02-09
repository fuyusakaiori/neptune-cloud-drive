package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.context.user.GetUserChildFileContext;
import com.neptune.cloud.drive.server.context.user.GetUserRootDirContext;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.vo.DirectoryTreeNodeVO;
import com.neptune.cloud.drive.server.vo.UploadChunkVO;
import com.neptune.cloud.drive.server.vo.UserFileVO;

import java.util.List;

public interface IUserFileService extends IService<UserFile> {

    long createUserDirectory(CreateUserDirectoryContext context);

    UserFile getUserRootDirectory(GetUserRootDirContext context);

    void deleteUserFile(DeleteUserFileContext context);

    List<UserFileVO> listUserFiles(ListUserFileContext context);

    void renameUserFile(RenameUserFileContext context);

    void secondUploadUserFile(SecondUploadUserFileContext context);

    void uploadUserFile(UploadUserFileContext context);

    boolean uploadUserFileChunk(UploadUserFileChunkContext context);

    List<UploadChunkVO> listUploadedUserFileChunk(GetUserFileChunkContext context);

    void mergeUploadedUserFileChunk(MergeUserFileChunkContext context);

    void downloadUserFile(DownloadUserFileContext context);

    void previewUserFile(PreviewUserFileContext context);

    List<DirectoryTreeNodeVO> listUserDirectoryTree(GetDirectoryTreeContext context);

    void transferUserFile(TransferUserFileContext context);

    void copyUserFile(CopyUserFileContext context);

    List<UserFile> selectUserChildFiles(GetUserChildFileContext context);

    List<UserFile> selectUserChildFiles(List<Long> fileIds);
}
