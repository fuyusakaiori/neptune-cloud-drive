package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件 ID
     */
    private long fileId;

    /**
     * 请求的响应结果
     */
    private HttpServletResponse response;
}
