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
public class DownloadFileContext {

    /**
     * 文件 ID
     */
    private long fileId;

    /**
     * 下载的文件
     */
    private HttpServletResponse response;

}
