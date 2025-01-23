package com.neptune.cloud.drive.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import lombok.Getter;

import java.io.Serializable;

/**
 * 公共响应结果
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    /**
     * 响应状态码
     */
    private final int code;

    /**
     * 响应描述
     */
    private final String message;

    /**
     * 响应结果
     */
    private final T body;

    public Response(int code) {
        this(code, StringConstant.EMPTY, null);
    }

    public Response(int code, String message) {
        this(code, message, null);
    }

    public Response(int code, String message, T body) {
        this.code = code;
        this.message = message;
        this.body = body;
    }

    /**
     * 避免 JSON 序列化时自动添加对应的属性
     */
    @JsonIgnore
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode() == this.code;
    }

    public static <T> Response<T> success() {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage());
    }

    public static <T> Response<T> success(String message) {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), message);
    }

    public static <T> Response<T> success(T body) {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), body);
    }

    public static <T> Response<T> fail() {
        return new Response<T>(ResponseCode.ERROR.getCode());
    }

    public static <T> Response<T> fail(String message) {
        return new Response<T>(ResponseCode.ERROR.getCode(), message);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<T>(code, message);
    }

}
