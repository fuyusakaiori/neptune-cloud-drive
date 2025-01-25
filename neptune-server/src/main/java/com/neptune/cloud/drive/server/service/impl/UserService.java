package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.common.constant.UserConstant;
import com.neptune.cloud.drive.server.context.file.CreateUserDirectoryContext;
import com.neptune.cloud.drive.server.context.user.*;
import com.neptune.cloud.drive.server.converter.UserConverter;
import com.neptune.cloud.drive.server.mapper.UserMapper;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.vo.UserInfoVO;
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
@Service
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
     * 忘记密码: 校验用户
     */
    @Override
    public String checkUsername(CheckUsernameContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 根据用户名查询密保问题: 只需要密保问题, 不需要整个用户信息
        String question = baseMapper.selectUserByUsername(context.getUsername());
        // 2. 判断密保问题是否存在
        if (StringUtils.isEmpty(question)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在");
        }
        // 3. 返回密保信息
        return question;
    }

    /**
     * 忘记密码: 校验密保答案
     */
    @Override
    public String checkAnswer(CheckAnswerContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断用户的密保问题答案是否正确
        doCheckAnswer(context.getUsername(), context.getQuestion(), context.getAnswer());
        // 2. 生成用户的临时的 token
        String accessToken = generateTemporaryAccessToken(context.getUsername());
        // 3. 判断 token 是否生成成功
        if (StringUtils.isEmpty(accessToken)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "校验密保问题答案失败");
        }
        return accessToken;
    }

    /**
     * 忘记密码: 重设密码
     */
    @Override
    public void resetPassword(ResetPasswordContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断用户是否可以修改密码
        checkResetPasswordToken(context.getUsername(), context.getToken());
        // 2. 更新旧密码为新的密码
        doResetPassword(context.getUsername(), context.getPassword());
    }

    /**
     * 更新用户密码
     */
    @Override
    public void changePassword(ChangePasswordContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断用户的旧密码是否正确
        checkOldPassword(context.getUserId(), context.getOldPassword());
        // 2. 更新用户的新密码
        doChangePassword(context.getUserId(), context.getNewPassword());
        // 3. 退出登录
        logout(new LogoutUserContext(context.getUserId()));
    }

    /**
     * 查询用户信息
     */
    @Override
    public UserInfoVO info(GetUserInfoContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询用户的基础信息
        User user = getById(context.getUserId());
        // 2. 判断是否查询成功
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在");
        }
        // 3. 查询用户关联的目录信息
        UserFile userRootDir = selectUserRootDirectory(context.getUserId());
        // 4. 判断是否查询成功
        if (Objects.isNull(userRootDir)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户根目录不存在");
        }
        return userConverter.assembleUserResponse(user, userRootDir);
    }

    //============================================= private =============================================

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
        CreateUserDirectoryContext context = new CreateUserDirectoryContext()
                .setUserId(user.getUserId())
                .setParentId(FileConstant.ROOT_PARENT_ID)
                .setDirectoryName(FileConstant.ROOT_PARENT_CN_NAME);
        // 3. 调用创建根目录的方法
        long id = userFileService.createUserDirectory(context);
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

    /**
     * 校验用户的密保问题
     */
    private void doCheckAnswer(String username, String question, String answer) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("username", username)
                .eq("question", question)
                .eq("answer", answer);
        // 1. 根据用户名、问题、答案直接查询
        int count = count(queryWrapper);
        // 2. 判断是否存在对应的用户信息
        if (count <= 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户密保答案错误");
        }
    }

    /**
     * 生成重置密码时的临时 token
     */
    private String generateTemporaryAccessToken(String username) {
        return JwtUtil.generateToken(
                username, UserConstant.USER_FORGET_USERNAME, username, UserConstant.USER_FORGET_TOKEN_EXPIRE_TIME);
    }

    /**
     * 校验用户重置密码使用的临时 token
     */
    private void checkResetPasswordToken(String username, String token) {
        // 1. 解析 token
        Object value = JwtUtil.analyzeToken(token, UserConstant.USER_FORGET_USERNAME);
        // 2. 判断 token 是否过期
        if (Objects.isNull(value)) {
            throw new BusinessException(ResponseCode.TOKEN_EXPIRE.getCode(), "超过重置密码的时限");
        }
        // 3. 判断用户账号是否相同
        if (!StringUtils.equals(String.valueOf(value), username)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户使用的临时 token 错误");
        }
    }

    /**
     * 校验用户旧密码是否正确, 并更新密码
     */
    private void doResetPassword(String username, String newPassword) {
        // 1. 根据用户账号查询用户信息
        User user = selectUser(username);
        // 2. 判断用户信息是否存在
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在");
        }
        try {
            // 3. 给新密码加密
            String encryption = PasswordUtil.encryptPassword(user.getSalt(), newPassword);
            // 4. 设置新的密码
            user = user.setPassword(encryption).setUpdateTime(new Date());
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重置用户密码失败");
        }
        // 5. 更新用户密码
        if (!updateById(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重置用户密码失败");
        }
    }

    /**
     * 判断更新密码时的旧密码是否正确
     */
    private void checkOldPassword(long userId, String oldPassword) {
        // 1. 查询用户信息
        User user = getById(userId);
        // 2. 判断用户是否存在
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在");
        }
        String encryption = StringConstant.EMPTY;
        try {
            // 3. 生成旧密码的散列值
            encryption = PasswordUtil.encryptPassword(user.getSalt(), oldPassword);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "更新用户密码失败");
        }
        // 4. 比较数据库中和输入的密码是否相同
        if (!StringUtils.equals(user.getPassword(), encryption)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "输入的旧密码错误");
        }
    }

    /**
     * 更新旧密码为新密码
     */
    private void doChangePassword(long userId, String newPassword) {
        // 1. 查询用户信息
        User user = getById(userId);
        // 2. 判断用户是否存在
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在");
        }
        String encryption = StringConstant.EMPTY;
        try {
            // 3. 生成新密码的散列值
            encryption = PasswordUtil.encryptPassword(user.getSalt(), newPassword);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "更新用户密码失败");
        }
        user.setPassword(encryption)
                .setUpdateTime(new Date());
        // 4. 更新用户密码
        if (!updateById(user)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "更新用户密码失败");
        }
    }

    /**
     * 查询用户根目录
     */
    private UserFile selectUserRootDirectory(long userId) {
        return userFileService.selectUserRootDirectory(
                new GetUserRootDirContext(userId));
    }

}




