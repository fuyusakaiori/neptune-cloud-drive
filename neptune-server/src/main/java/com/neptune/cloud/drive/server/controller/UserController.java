package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.converter.UserConverter;
import com.neptune.cloud.drive.server.request.RegisterUserRequest;
import com.neptune.cloud.drive.server.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Response<Long> register(@Validated @RequestBody RegisterUserRequest request) {
        log.info("UserController register: 开始注册用户信息, request = {}", request);
        // 1. 请求转换为中间类
        RegisterUserContext context = userConverter.userRegisterRequest2UserRegisterContext(request);
        // 2. 调用用户注册逻辑的方法
        long id = userService.register(context);
        // 3. 判断返回结果是否存在问题
        if (id < 0) {
            log.error("UserController register: 注册用户信息失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        log.info("UserController register: 注册用户信息结束, request = {}, id = {}", request, id);
        return Response.success(id);
    }

}
