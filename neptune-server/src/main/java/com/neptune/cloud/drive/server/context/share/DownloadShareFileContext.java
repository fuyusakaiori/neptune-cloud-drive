package com.neptune.cloud.drive.server.context.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadShareFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 分享链接 ID
     */
    private long shareId;

    /**
     * 分享文件 ID
     */
    private long shareFileId;

    /**
     * 响应结果
     */
    private HttpServletResponse response;

}
