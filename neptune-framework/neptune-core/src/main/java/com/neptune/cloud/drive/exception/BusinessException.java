package com.neptune.cloud.drive.exception;

import lombok.Getter;

/**
 * 自定义全局异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 状态码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;


    public BusinessException(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
