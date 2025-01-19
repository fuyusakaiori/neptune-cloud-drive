package com.neptune.cloud.drive.exception;

import lombok.Getter;

/**
 * 自定义全局异常
 */
@Getter
public class GlobalRuntimeException extends RuntimeException {

    /**
     * 错误信息
     */
    private final String message;


    public GlobalRuntimeException(String message) {
        this.message = message;
    }

}
