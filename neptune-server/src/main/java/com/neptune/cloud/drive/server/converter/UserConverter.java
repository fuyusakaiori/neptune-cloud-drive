package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.LoginUserContext;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.request.LoginUserRequest;
import com.neptune.cloud.drive.server.request.RegisterUserRequest;
import org.mapstruct.Mapper;

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
     * UserRegisterContext => User
     */
    User userRegisterContext2User(RegisterUserContext context);

}
