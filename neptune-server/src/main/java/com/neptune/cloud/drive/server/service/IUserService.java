package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.model.User;

public interface IUserService extends IService<User> {

    long register(RegisterUserContext context);

}
