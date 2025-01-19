package com.neptune.cloud.drive.filter.log;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "logFilter")
@Order(Integer.MAX_VALUE)
public class HttpLogFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 1. 请求计时
        StopWatch stopWatch = StopWatch.createStarted();
        // 2. 封装请求响应, 避免影响到原有的请求响应
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        // 3. 执行请求
        chain.doFilter(requestWrapper, responseWrapper);
        // 4. 记录日志
        HttpLogEntity httpLogEntity = HttpLogEntityBuilder.build(requestWrapper, responseWrapper, stopWatch);
        httpLogEntity.log();
        // 5. 将响应结果从包装类拷贝到真实的响应
        responseWrapper.copyBodyToResponse();
    }
}
