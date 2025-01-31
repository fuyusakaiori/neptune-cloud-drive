package com.neptune.cloud.drive.server.common.aspect;

import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.annotation.LoginIgnore;
import com.neptune.cloud.drive.server.common.constant.HttpConstant;
import com.neptune.cloud.drive.server.common.constant.UserConstant;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 登录鉴权切面
 */
@Slf4j
@Component
@Aspect
public class LoginAspect {

    /**
     * 切面逻辑
     */
    private final static String POINT_CUT = "execution(* com.neptune.cloud.drive.server.controller..*(..))";

    @Autowired
    private CacheManager cacheManager;

    @Pointcut(value = POINT_CUT)
    public void checkLogin() {

    }

    @Around(value = "checkLogin()")
    public Object checkLoginAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 1. 判断方法是否需要登录校验
        if (!isNeedCheckLogin(proceedingJoinPoint)) {
            return proceedingJoinPoint.proceed();
        }
        // 2. 获取请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 3. 判断请求是否为空
        if (Objects.isNull(attributes)) {
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 获取 HTTP 请求
        HttpServletRequest request = attributes.getRequest();
        // 5. 判断用户是否登录
        if (!doCheckLogin(request)) {
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        return proceedingJoinPoint.proceed();
    }

    /**
     * 判断是否需要登录校验
     */
    private boolean isNeedCheckLogin(ProceedingJoinPoint proceedingJoinPoint) {
        // 1. 获取方法签名
        Signature signature = proceedingJoinPoint.getSignature();
        // 2. 强制转换为方法
        if (signature instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) signature;
            // 3. 获取方法
            Method method = methodSignature.getMethod();
            // 4. 获取方法的注解
            return !method.isAnnotationPresent(LoginIgnore.class);
        }
        return true;
    }

    /**
     * 判读是否登录
     */
    private boolean doCheckLogin(HttpServletRequest request) {
        // 1. 从请求头中获取 token
        String loginToken = request.getHeader(HttpConstant.HTTP_AUTHORIZATION);
        // 2. 判断请求头中是否携带 token
        if (StringUtils.isEmpty(loginToken)) {
            // 4. 尝试再从携带的请求参数中获取 token
            loginToken = request.getParameter(HttpConstant.HTTP_AUTHORIZATION);
            // 5. 再次判断是否获取到 token
            if (StringUtils.isEmpty(loginToken)) {
                return false;
            }
        }
        // 6. 解析 token
        Object userId = JwtUtil.analyzeToken(loginToken, UserConstant.USER_LOGIN_ID);
        // 7. 判断解析是否成功
        if (Objects.isNull(userId)) {
            return false;
        }
        // 8. 获取缓存
        Cache cache = cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        // 9. 判断缓存是否为空
        if (Objects.isNull(cache)) {
            return false;
        }
        // 10. 获取缓存中的 token: 如果没有使用缓存, 就需要根据用户 ID 在数据库中查询
        String accessToken = cache.get(CacheConstant.USER_LOGIN_PREFIX + userId, String.class);
        // 11. 比较 token 是否相同
        if (!StringUtils.equals(loginToken, accessToken)) {
            return false;
        }
        // 12. 如果 token 相同, 那么保存用户 ID 在上下文中
        UserThreadLocal.set(Long.parseLong(String.valueOf(userId)));

        return true;
    }


}
