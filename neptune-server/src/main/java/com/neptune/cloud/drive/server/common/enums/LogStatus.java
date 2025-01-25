package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogStatus {

    UNPROCESSED(0),

    PROCESS(1);

    private int status;

}
