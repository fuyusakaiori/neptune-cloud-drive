package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.model.User;
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
    RegisterUserContext userRegisterRequest2UserRegisterContext(RegisterUserRequest request);

    /**
     * UserRegisterContext => User
     */
    User userRegisterContext2User(RegisterUserContext context);

}
