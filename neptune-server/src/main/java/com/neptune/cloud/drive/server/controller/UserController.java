package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.annotation.LoginIgnore;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.context.LoginUserContext;
import com.neptune.cloud.drive.server.context.LogoutUserContext;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.converter.UserConverter;
import com.neptune.cloud.drive.server.request.LoginUserRequest;
import com.neptune.cloud.drive.server.request.RegisterUserRequest;
import com.neptune.cloud.drive.server.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Api(value = "用户模块")
@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private UserConverter userConverter;

    /**
     * 用户注册
     */
    @ApiOperation(
            value = "用户注册接口",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping(value = "/register")
    @LoginIgnore
    public Response<Long> register(@Validated @RequestBody RegisterUserRequest request) {
        log.info("UserController register: 开始注册用户信息, request = {}", request);
        // 1. 请求转换为中间类
        RegisterUserContext context = userConverter.registerUserRequest2RegisterUserContext(request);
        // 2. 调用用户注册逻辑的方法
        long id = userService.register(context);
        // 3. 判断返回结果是否存在问题
        if (id < 0) {
            log.error("UserController register: 注册用户信息失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "用户注册失败");
        }
        log.info("UserController register: 注册用户信息结束, request = {}, id = {}", request, id);
        return Response.success(id);
    }

    /**
     * 用户登录
     */
    @ApiOperation(
            value = "用户登录接口",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/login")
    @LoginIgnore
    public Response<String> login(@Validated @RequestBody LoginUserRequest request) {
        log.info("UserController login: 开始执行用户登录, request = {}", request);
        // 1. 请求转换为上下文
        LoginUserContext context = userConverter.loginUserRequest2LoginUserContext(request);
        // 2. 调用用户登录逻辑的方法
        String accessToken = userService.login(context);
        // 3. 判断是否登录成功
        if (StringUtils.isEmpty(accessToken)) {
            log.error("UserController login: 用户登录失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "用户登录失败");
        }
        log.info("UserController login: 结束执行用户登录, request = {}, accessToken = {}", request, accessToken);
        return Response.success(accessToken);
    }

    /**
     * 用户登出
     */
    @ApiOperation(
            value = "用户登出接口",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/logout")
    public Response<Void> logout() {
        log.info("UserController logout: 开始执行用户登出...");
        // 1. 从线程本地变量获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断是否获取到用户 ID
        if (Objects.isNull(userId)) {
            log.error("UserController logout: 用户未登录, 不允许直接登出");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 封装登出的上下文请求
        LogoutUserContext context = new LogoutUserContext().setUserId(userId);
        // 4. 调用用户登出逻辑的方法
        userService.logout(context);
        // 5. 登出结束
        log.info("UserController logout: 结束执行用户登出");
        return Response.success();
    }

}
