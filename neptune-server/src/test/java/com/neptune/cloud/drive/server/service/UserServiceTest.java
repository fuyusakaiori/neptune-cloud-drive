package com.neptune.cloud.drive.server.service;

import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.server.CloudDriveBootstrap;
import com.neptune.cloud.drive.server.common.constant.UserConstant;
import com.neptune.cloud.drive.server.context.LoginUserContext;
import com.neptune.cloud.drive.server.context.LogoutUserContext;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.util.JwtUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = CloudDriveBootstrap.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceTest {

    @Autowired
    private IUserService userService;

    @Test(expected = BusinessException.class)
    public void registerUserTest() {
        RegisterUserContext context = registerUserContext();
        // 1. 注册用户
        long register = userService.register(context);
        // 2. 判断是否注册成功
        Assert.assertTrue(register > 0);
        // 3. 重复注册
        register = userService.register(context);
    }

    @Test
    public void loginSuccessTest() {
        LoginUserContext context = loginUserContext();
        // 1. 登录用户
        String accessToken = userService.login(context);
        // 2. 判断是否登录成功
        Assert.assertNotNull(accessToken);
    }

    @Test(expected = BusinessException.class)
    public void loginFailTest() {
        LoginUserContext context = loginUserContext();
        // 1. 修改为错误的密码
        context.setPassword("123456789");
        // 2. 登录用户
        String accessToken = userService.login(context);
        // 3. 判断是否登录成功
        Assert.assertNotNull(accessToken);
    }

    @Test
    public void logoutSuccessTest() {
        RegisterUserContext registerContext = registerUserContext();
        // 1. 注册用户
        long register = userService.register(registerContext);
        // 2. 判断是否注册成功
        Assert.assertTrue(register > 0);

        LoginUserContext loginContext = loginUserContext();
        // 3. 登录用户
        String accessToken = userService.login(loginContext);
        // 4. 判断是否登录成功
        Assert.assertNotNull(accessToken);

        // 5. 解析 token
        Long userId = (Long) JwtUtil.analyzeToken(accessToken, UserConstant.USER_LOGIN_ID);

        Assert.assertNotNull(userId);

        LogoutUserContext logoutContext = logoutUserContext().setUserId(1882329189213491200L);
        // 6. 登出用户
        userService.logout(logoutContext);
    }

    private RegisterUserContext registerUserContext() {
        return new RegisterUserContext()
                .setUsername("fuyusakaiori")
                .setPassword("123456")
                .setQuestion("最喜欢的动画")
                .setAnswer("吹响!上低音号!");
    }

    private LoginUserContext loginUserContext() {
        return new LoginUserContext()
                .setUsername("fuyusakaiori").setPassword("123456");
    }

    private LogoutUserContext logoutUserContext() {
        return new LogoutUserContext();
    }

}
