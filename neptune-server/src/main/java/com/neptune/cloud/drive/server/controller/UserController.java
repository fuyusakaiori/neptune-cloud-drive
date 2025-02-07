package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.annotation.LoginIgnore;
import com.neptune.cloud.drive.server.context.user.*;
import com.neptune.cloud.drive.server.request.user.*;
import com.neptune.cloud.drive.server.vo.UserInfoVO;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.converter.UserConverter;
import com.neptune.cloud.drive.server.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
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
    @PostMapping(value = "/user/register")
    @LoginIgnore
    public Response<Long> registerUser(@Validated @RequestBody RegisterUserRequest request) {
        log.info("UserController v: 开始注册用户信息, request = {}", request);
        // 1. 请求转换为中间类
        RegisterUserContext context = userConverter.registerUserRequest2RegisterUserContext(request);
        // 2. 调用用户注册逻辑的方法
        long id = userService.register(context);
        // 3. 判断返回结果是否存在问题
        if (id < 0) {
            log.error("UserController registerUser: 注册用户信息失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "用户注册失败");
        }
        log.info("UserController registerUser: 注册用户信息结束, request = {}, id = {}", request, id);
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
    @PostMapping("/user/login")
    @LoginIgnore
    public Response<String> loginUser(@Validated @RequestBody LoginUserRequest request) {
        log.info("UserController loginUser: 开始执行用户登录, request = {}", request);
        // 1. 请求转换为上下文
        LoginUserContext context = userConverter.loginUserRequest2LoginUserContext(request);
        // 2. 调用用户登录逻辑的方法
        String accessToken = userService.login(context);
        // 3. 判断是否登录成功
        if (StringUtils.isEmpty(accessToken)) {
            log.error("UserController loginUser: 用户登录失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "用户登录失败");
        }
        log.info("UserController loginUser: 结束执行用户登录, request = {}, accessToken = {}", request, accessToken);
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
    public Response<Void> logoutUser() {
        log.info("UserController logoutUser: 开始执行用户登出...");
        // 1. 从线程本地变量获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断是否获取到用户 ID
        if (Objects.isNull(userId)) {
            log.error("UserController logoutUser: 用户未登录, 不允许直接登出");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 封装登出的上下文请求
        LogoutUserContext context = new LogoutUserContext().setUserId(userId);
        // 4. 调用用户登出逻辑的方法
        userService.logout(context);
        // 5. 登出结束
        log.info("UserController logoutUser: 结束执行用户登出");
        return Response.success();
    }

    /**
     * 忘记密码: 校验用户名
     */
    @ApiOperation(
            value = "忘记密码: 校验用户名",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/username/check")
    @LoginIgnore
    public Response<String> checkUsername(@Validated @RequestBody CheckUsernameRequest request) {
        log.info("UserController checkUsername: 忘记密码时校验用户账号开始执行, request = {}", request);
        // 1. 请求转换为上下文
        CheckUsernameContext context = userConverter.checkUsernameRequest2CheckUsernameContext(request);
        // 2. 调用忘记密码时校验用户名的方法
        String question = userService.checkUsername(context);
        // 3. 判断是否校验成功
        if (StringUtils.isEmpty(question)) {
            log.info("UserController checkUsername: 忘记密码时校验用户账号执行失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "校验用户信息失败");
        }
        // 4. 返回用户关联的密保问题
        log.info("UserController checkUsername: 忘记密码时校验用户账号执行结束, request = {}, question = {}", request, question);
        return Response.success(question);
    }

    /**
     * 忘记密码: 校验密保答案
     */
    @ApiOperation(
            value = "忘记密码: 校验用户名",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/answer/check")
    @LoginIgnore
    public Response<String> checkAnswer(@Validated @RequestBody CheckAnswerRequest request) {
        log.info("UserController checkAnswer: 忘记密码时校验密保问题答案开始执行, request = {}", request);
        // 1. 请求转换为上下文
        CheckAnswerContext context = userConverter.checkAnswerRequest2CheckAnswerContext(request);
        // 2. 调用忘记密码时校验用户密保问题答案的方法
        String accessToken = userService.checkAnswer(context);
        // 3. 判断是否校验成功
        if (StringUtils.isEmpty(accessToken)) {
            log.info("UserController checkAnswer: 忘记密码时校验密保问题答案执行失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "校验密保问题答案失败");
        }
        // 4. 返回用户临时登录的 token
        log.info("UserController checkAnswer: 忘记密码时校验密保问题答案执行结束, request = {}, token = {}", request, accessToken);
        return Response.success(accessToken);
    }

    /**
     * 忘记密码: 重置密码
     */
    @ApiOperation(
            value = "忘记密码: 重置密码",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/password/reset")
    @LoginIgnore
    public Response<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        log.info("UserController resetPassword: 忘记密码时重置密码开始执行, request = {}", request);
        // 1. 请求转换为上下文
        ResetPasswordContext context = userConverter.resetPasswordRequest2ResetPasswordContext(request);
        // 2. 调用忘记密码时重设密码的方法
        userService.resetPassword(context);
        log.info("UserController resetPassword: 忘记密码时重置密码执行结束, request = {}", request);
        return Response.success();
    }

    /**
     * 更新密码
     */
    @ApiOperation(
            value = "更新密码",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/password/change")
    public Response<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        log.info("UserController changePassword: 更新密码开始执行, request = {}", request);
        // 1. 请求转换为上下文
        ChangePasswordContext context = userConverter.changePasswordRequest2ChangePasswordContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserController changePassword: 用户未登录, 不允许更新密码");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 在上下文中设置用户 ID
        context.setUserId(userId);
        // 5. 调用忘记密码时重设密码的方法
        userService.changePassword(context);
        log.info("UserController changePassword: 更新密码, request = {}", request);
        return Response.success();
    }

    /**
     * 查询用户信息
     */
    @ApiOperation(
            value = "查询用户信息",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/info")
    public Response<UserInfoVO> infoUser() {
        log.info("UserController infoUser: 开始查询用户的基本信息");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 调用查询用户信息的方法
        UserInfoVO userInfo = userService.infoUser(new GetUserInfoContext(userId));
        // 4. 返回用户信息
        log.info("UserController infoUser: 查询用户的基本信息结束, user = {}", userInfo);
        return Response.success(userInfo);
    }

}
