package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.*;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.request.*;
import com.neptune.cloud.drive.server.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 转换工具
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * UserRegisterRequest => UserRegisterContext
     */
    RegisterUserContext registerUserRequest2RegisterUserContext(RegisterUserRequest request);

    /**
     * LoginUserRequest => LoginUserContext
     */
    LoginUserContext loginUserRequest2LoginUserContext(LoginUserRequest request);

    /**
     * CheckUsernameRequest => CheckUsernameContext
     */
    CheckUsernameContext checkUsernameRequest2CheckUsernameContext(CheckUsernameRequest request);

    /**
     * CheckAnswerRequest => CheckAnswerContext
     */
    CheckAnswerContext checkAnswerRequest2CheckAnswerContext(CheckAnswerRequest request);

    /**
     * ResetPasswordRequest => ResetPasswordContext
     */
    ResetPasswordContext resetPasswordRequest2ResetPasswordContext(ResetPasswordRequest request);

    /**
     * ChangePasswordRequest => ChangePasswordContext
     */
    ChangePasswordContext changePasswordRequest2ChangePasswordContext(ChangePasswordRequest request);

    /**
     * UserRegisterContext => User
     */
    User userRegisterContext2User(RegisterUserContext context);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "dir.fileId", target = "rootDirId")
    @Mapping(source = "dir.filename", target = "rootDirName")
    UserResponse assembleUserResponse(User user, UserFile dir);

}
