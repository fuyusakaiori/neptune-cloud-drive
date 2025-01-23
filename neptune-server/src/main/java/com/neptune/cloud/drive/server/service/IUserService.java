package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.*;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.response.UserResponse;

public interface IUserService extends IService<User> {

    long register(RegisterUserContext context);

    String login(LoginUserContext context);

    void logout(LogoutUserContext context);

    String checkUsername(CheckUsernameContext context);

    String checkAnswer(CheckAnswerContext context);

    void resetPassword(ResetPasswordContext context);

    void changePassword(ChangePasswordContext context);

    UserResponse info(GetUserInfoContext context);
}
