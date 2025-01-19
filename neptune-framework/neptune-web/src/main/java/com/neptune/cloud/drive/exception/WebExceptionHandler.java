package com.neptune.cloud.drive.exception;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class WebExceptionHandler {

    /**
     * 业务异常处理器
     */
    @ExceptionHandler(value = BusinessException.class)
    public Response<?> businessExceptionHandler(BusinessException exception) {
        return Response.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 参数校验失败异常处理器
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Response<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {
        // 1. 因为采用的校验策略是快速失败, 所以只需要获取第一个错误
        ObjectError error = exception.getBindingResult().getAllErrors().stream().findFirst().get();
        // 2. 返回响应结果
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), error.getDefaultMessage());
    }

    /**
     * 参数的约束校验失败的异常处理器 @Size
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Response<?> constraintViolationExceptionHandler(ConstraintViolationException exception) {
        ConstraintViolation<?> error = exception.getConstraintViolations().stream().findFirst().get();
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), error.getMessage());
    }

    /**
     * 缺少必要参数时的异常处理器 @RequestParam(required=true)
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Response<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException exception) {
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), exception.getMessage());
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public Response<?> illegalStateExceptionHandler(IllegalStateException exception) {
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), exception.getMessage());
    }

    /**
     * 参数绑定错误异常处理器
     */
    @ExceptionHandler(value = BindException.class)
    public Response<?> bindExceptionHandler(BindException exception) {
        ObjectError error = exception.getAllErrors().stream().findFirst().get();
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), error.getDefaultMessage());
    }


    @ExceptionHandler(value = RuntimeException.class)
    public Response<?> runtimeExceptionHandler(BindException exception) {
        ObjectError error = exception.getAllErrors().stream().findFirst().get();
        return Response.fail(ResponseCode.ERROR_PARAM.getCode(), error.getDefaultMessage());
    }

}
