package com.neptune.cloud.drive.server.listener;

import com.neptune.cloud.drive.server.common.enums.LogStatus;
import com.neptune.cloud.drive.server.common.event.RecordErrorLogEvent;
import com.neptune.cloud.drive.server.model.ErrorLog;
import com.neptune.cloud.drive.server.service.IErrorLogService;
import com.neptune.cloud.drive.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class ErrorLogEventListener {

    @Autowired
    private IErrorLogService errorLogService;

    /**
     * 监听记录日志的事件
     */
    @EventListener(RecordErrorLogEvent.class)
    public void recordErrorLog(RecordErrorLogEvent event) {
        if (Objects.isNull(event) || event.getUserId() <= 0 || StringUtils.isEmpty(event.getMessage())) {
            return;
        }
        // 1. 封装错误日志记录
        ErrorLog errorLog = assembleErrorLog(event.getUserId(), event.getMessage());
        // 2. 记录错误日志
        if (errorLogService.save(errorLog)) {
            log.error("ErrorLogEventListener recordErrorLog: 记录错误日志出现异常, log = {}", errorLog);
        }
    }

    private ErrorLog assembleErrorLog(long userId, String message) {
        return new ErrorLog()
                .setLogId(IdUtil.generate())
                .setLogContent(message)
                .setLogStatus(LogStatus.UNPROCESSED.getStatus())
                .setCreateUser(userId)
                .setCreateTime(new Date())
                .setUpdateUser(userId)
                .setUpdateTime(new Date());
    }

}
