package com.neptune.cloud.drive.server.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class ServerStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) {
        // 1. 获取端口号
        String port = readyEvent.getApplicationContext().getEnvironment().getProperty("server.port");
        // 2. 生成 URL 地址
        String url = String.format("http://%s:%s", "127.0.0.1", port);
        // 3. 记录日志
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "neptune cloud drive start at: ", url));
        // 4. 判断是否开启接口文档生成
        if (checkShowServerApiDoc(readyEvent)) {
            log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "neptune cloud drive's doc start at: %v", url + "/doc.html"));
        }
        // 5. 记录启动日志
        log.info(AnsiOutput.toString(AnsiColor.GREEN, "neptune cloud drive has started successfully!"));
    }

    private boolean checkShowServerApiDoc(ApplicationReadyEvent readyEvent) {
        return readyEvent.getApplicationContext().getEnvironment().getProperty("swagger2.show", Boolean.class, true)
                && readyEvent.getApplicationContext().containsBean("swaggerConfig");
    }

}
