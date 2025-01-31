package com.neptune.cloud.drive.server.common.aspect;

import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.annotation.LoginIgnore;
import com.neptune.cloud.drive.server.common.constant.HttpConstant;
import com.neptune.cloud.drive.server.common.constant.ShareConstant;
import com.neptune.cloud.drive.server.common.constant.UserConstant;
import com.neptune.cloud.drive.server.threadlocal.ShareThreadLocal;
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

@Slf4j
@Component
@Aspect
public class ShareAspect {


    /**
     * 切面逻辑
     */
    private final static String POINT_CUT = "@annotation(com.neptune.cloud.drive.server.common.annotation.NeedShareCode)";

    @Autowired
    private CacheManager cacheManager;

    @Pointcut(value = POINT_CUT)
    public void checkShareCode() {

    }

    @Around(value = "checkShareCode()")
    public Object checkShareAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 1. 获取请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 2. 判断请求是否为空
        if (Objects.isNull(attributes)) {
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 3. 获取 HTTP 请求
        HttpServletRequest request = attributes.getRequest();
        // 4.
        if (!doCheckShareCode(request)) {
            return Response.fail(ResponseCode.ACCESS_DENIED.getCode(), ResponseCode.ACCESS_DENIED.getMessage());
        }
        return proceedingJoinPoint.proceed();
    }

    /**
     * 判读是否登录
     */
    private boolean doCheckShareCode(HttpServletRequest request) {
        // 1. 从请求头中获取 token
        String shareToken = request.getHeader(HttpConstant.HTTP_SHARE_TOKEN);
        // 2. 判断请求头中是否携带 token
        if (StringUtils.isEmpty(shareToken)) {
            // 3. 尝试再从携带的请求参数中获取 token
            shareToken = request.getParameter(HttpConstant.HTTP_SHARE_TOKEN);
            // 4. 再次判断是否获取到 token
            if (StringUtils.isEmpty(shareToken)) {
                return false;
            }
        }
        // 5. 解析 token
        Object shareId = JwtUtil.analyzeToken(shareToken, ShareConstant.SHARE_TOKEN_KEY);
        // 6. 判断解析是否成功
        if (Objects.isNull(shareId)) {
            return false;
        }
        // 7. 获取缓存
        Cache cache = cacheManager.getCache(CacheConstant.CLOUD_DRIVE_CACHE_NAME);
        // 8. 判断缓存是否为空
        if (Objects.isNull(cache)) {
            return false;
        }
        // 9. 获取缓存中的 token: 如果没有使用缓存, 就需要根据用户 ID 在数据库中查询
        String accessToken = cache.get(CacheConstant.SHARE_TOKEN_PREFIX + shareId, String.class);
        // 10. 比较 token 是否相同
        if (!StringUtils.equals(shareToken, accessToken)) {
            return false;
        }
        // 11. 如果 token 相同, 那么保存分享链接 ID 在上下文中
        ShareThreadLocal.set(Long.parseLong(String.valueOf(shareId)));

        return true;
    }

}
