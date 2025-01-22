package com.neptune.cloud.drive.server.service;

import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.server.CloudDriveBootstrap;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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



    private RegisterUserContext registerUserContext() {
        return new RegisterUserContext()
                .setUsername("fuyusakaiori")
                .setPassword("123456")
                .setQuestion("最喜欢的动画")
                .setAnswer("吹响!上低音号!");
    }


}
