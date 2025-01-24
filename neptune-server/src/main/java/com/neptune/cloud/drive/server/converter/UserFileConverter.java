package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.file.CreateUserDirectoryContext;
import com.neptune.cloud.drive.server.context.file.DeleteUserFileContext;
import com.neptune.cloud.drive.server.context.file.RenameUserFileContext;
import com.neptune.cloud.drive.server.context.file.SecondUploadUserFileContext;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.request.file.CreateUserDirectoryRequest;
import com.neptune.cloud.drive.server.request.file.DeleteUserFileRequest;
import com.neptune.cloud.drive.server.request.file.RenameUserFileRequest;
import com.neptune.cloud.drive.server.request.file.SecondUploadUserFileRequest;
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
     * UserFile => UserFileVO
     */
    UserFileVO userFile2UserFileVO(UserFile userFile);


}
