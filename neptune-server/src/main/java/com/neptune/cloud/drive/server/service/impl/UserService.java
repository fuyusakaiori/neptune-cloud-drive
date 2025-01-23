package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.common.constant.UserConstant;
import com.neptune.cloud.drive.server.context.CreateUserFolderContext;
import com.neptune.cloud.drive.server.context.LoginUserContext;
import com.neptune.cloud.drive.server.context.LogoutUserContext;
import com.neptune.cloud.drive.server.context.RegisterUserContext;
import com.neptune.cloud.drive.server.converter.UserConverter;
import com.neptune.cloud.drive.server.mapper.UserMapper;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.service.IUserService;
import com.neptune.cloud.drive.util.IdUtil;
import com.neptune.cloud.drive.util.JwtUtil;
import com.neptune.cloud.drive.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service(value = "userService")
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private CacheManager cacheManager;


    /**
     * 用户注册
     */
    @Override
    public long register(RegisterUserContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 封装实体类
        User user = assembleUser(context);
        // 2. 创建用户信息
        doRegister(user);
        // 3. 创建用户的文件根目录
        createUserRootFolder(user);

        return user.getUserId();
    }

    /**
     * 用户登录
     */
    @Override
    public String login(LoginUserContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 验证登录信息
        checkLogin(context);
        // 2. 生成登录的 token 信息
        String accessToken = generateAccessToken(context.getUserId(), context.getUsername());
        // 3. 判断 token 是否生成成功
        if (StringUtils.isEmpty(accessToken)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户登录失败");
        }
        return accessToken;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(LogoutUserContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 获取缓存
        Cache cache = cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        // 2. 判断缓存是否获取成功
        if (Objects.isNull(cache)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 3. 清除缓存
        cache.evict(CacheConstant.USER_LOGIN_PREFIX + context.getUserId());
    }

    /**
     * 封装用户实体
     */
    private User assembleUser(RegisterUserContext context) {
        String salt = StringConstant.EMPTY, encryption = StringConstant.EMPTY;
        // 1. 将用户上下文信息封装到用户实体中
        User user = userConverter.userRegisterContext2User(context);
        try {
            // 2. 生成用于加密的盐值
            salt = PasswordUtil.generateSalt();
            // 3. 生成加密的密码
            encryption = PasswordUtil.encryptPassword(salt, user.getPassword());
        } catch (NoSuchAlgorithmException exception) {
            log.error("UserService assembleUserEntity: 加密用户密码出现异常, context = {}", context, exception);
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 4. 设置用户实体的信息
        return user.setUserId(IdUtil.generate())
                .setPassword(encryption)
                .setSalt(salt)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
    }

    /**
     * 注册用户
     */
    private void doRegister(User user) {
        // 1. 判断用户实体是否为空
        if (Objects.isNull(user)) {
            log.error("UserService doRegister: 用户实体信息为空, 无法注册用户信息");
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 注册用户
        try {
            if (!save(user)) {
                log.error("UserService doRegister: 用户信息注册失败, user = {}", user);
                throw new BusinessException(ResponseCode.ERROR.getCode(), "用户注册失败");
            }
        } catch (DuplicateKeyException exception) {
            log.error("UserService doRegister: 用户已经存在, 不允许重复注册, user = {}", user);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户已经存在");
        }
    }

    /**
     * 创建用户关联的根目录
     */
    private void createUserRootFolder(User user) {
        // 1. 判断用户实体是否巍峨空
        if (Objects.isNull(user)) {
            log.error("UserService createUserRootFolder: 用户实体信息为空, 无法创建用户根目录");
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 封装创建根目录的上下文
        CreateUserFolderContext context = new CreateUserFolderContext()
                .setUserId(user.getUserId())
                .setParentId(FileConstant.ROOT_PARENT_ID)
                .setFolderName(FileConstant.ROOT_PARENT_CN_NAME);
        // 3. 调用创建根目录的方法
        long id = userFileService.createUserFolder(context);
        // 4. 判断是否创建成功
        if (id < 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户根目录创建失败");
        }
    }

    /**
     * 验证用户登录信息
     */
    private void checkLogin(LoginUserContext context) {
        // 1. 判断账号密码是否为空
        if (StringUtils.isAnyEmpty(context.getUsername(), context.getPassword())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 查询用户登录信息
        User user = selectUser(context.getUsername());
        // 3. 判断查询是否成功
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户账号不存在");
        }
        try {
            // 4. 判断用户登录的密码是否正确
            String encryptPassword = user.getPassword();
            String loginPassword = PasswordUtil.encryptPassword(user.getSalt(), context.getPassword());
            if (!encryptPassword.equals(loginPassword)) {
                log.error("UserService checkLogin: 用户登录失败, 输入的账号密码错误, context = {}", context);
                throw new BusinessException(ResponseCode.ERROR.getCode(), "用户登录的密码错误");
            }
            // NOTE: 在上下文中设置用户 ID, 用于生成 token
            context.setUserId(user.getUserId());
        } catch (NoSuchAlgorithmException exception) {
            log.error("UserService checkLogin: 生成登录密码的散列值出现异常, context = {}", context, exception);
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
    }

    /**
     * 根据用户名查询用户信息
     */
    private User selectUser(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("username", username);
        return getOne(queryWrapper);
    }

    /**
     * 生成用户登录的 token
     */
    private String generateAccessToken(long userId, String username) {
        // 1. 生成登录 token
        String accessToken = JwtUtil.generateToken(
                username, UserConstant.USER_LOGIN_ID, userId, UserConstant.USER_LOGIN_TOKEN_EXPIRE_TIME);
        // 2. 获取应用缓存
        Cache cache = cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        // 3. 判断缓存是否获取成功
        if (Objects.isNull(cache)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "缓存用户登录 token 失败");
        }
        // 4. 缓存用户登录 token: 无法单独设置 key-value 过期时间, 只能通过配置设置
        cache.put(CacheConstant.USER_LOGIN_PREFIX + userId, accessToken);

        return accessToken;
    }


}




