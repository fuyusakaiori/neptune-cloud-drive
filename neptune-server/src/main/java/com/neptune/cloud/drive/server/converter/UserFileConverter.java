package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.request.file.*;
import com.neptune.cloud.drive.server.vo.DirectoryTreeNodeVO;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserFileConverter {

    /**
     * CreateUserDirectoryRequest => CreateUserDirectoryContext
     */
    @Mapping(target = "parentId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getParentId())")
    CreateUserDirectoryContext createUserDirectoryRequest2CreateUserDirectoryContext(CreateUserDirectoryRequest request);

    /**
     * RenameUserFileRequest => RenameUserFileContext
     */
    @Mapping(target = "fileId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getFileId())")
    RenameUserFileContext renameUserFileRequest2RenameUserFileContext(RenameUserFileRequest request);

    /**
     * DeleteUserFileRequest => DeleteUserFileContext
     */
    DeleteUserFileContext deleteUserFileRequest2DeleteUserFileContext(DeleteUserFileRequest request);

    /**
     * SecondUploadUserFileRequest => SecondUploadUserFileContext
     */
    @Mapping(target = "parentId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getParentId())")
    SecondUploadUserFileContext secondUploadUserFileRequest2SecondUploadUserFileContext(SecondUploadUserFileRequest request);

    /**
     * UploadFileRequest => UploadFileContext
     */
    @Mapping(target = "parentId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getParentId())")
    UploadUserFileContext uploadUserFileRequest2UploadUserFileContext(UploadUserFileRequest request);

    /**
     * UploadUserFileChunkRequest => UploadUserFileChunkContext
     */
    @Mapping(target = "parentId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getParentId())")
    UploadUserFileChunkContext uploadUserFileChunkRequest2UploadUserFileChunkContext(UploadUserFileChunkRequest request);

    /**
     * GetUserFileChunkRequest => GetUserFileChunkContext
     */
    GetUserFileChunkContext getUserFileChunkReqest2GetUserFileChunkContext(GetUserFileChunkRequest request);

    /**
     * MergeUserFileChunkRequest => MergeUserFileChunkContext
     */
    @Mapping(target = "parentId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getParentId())")
    MergeUserFileChunkContext mergeUserFileChunkRequest2MergeUserFileChunkContext(MergeUserFileChunkRequest request);


    /**
     * UserFile => UserFileVO
     */
    UserFileVO userFile2UserFileVO(UserFile userFile);

    /**
     * UserFile => DirectoryTreeNodeVO
     */
    @Mapping(target = "directoryId", source = "userFile.fileId")
    @Mapping(target = "directoryName", source = "userFile.filename")
    @Mapping(target = "children", expression = "java(com.google.common.collect.Lists.newArrayList())")
    DirectoryTreeNodeVO UserFile2DirectoryTreeNodeVO(UserFile userFile);
}
