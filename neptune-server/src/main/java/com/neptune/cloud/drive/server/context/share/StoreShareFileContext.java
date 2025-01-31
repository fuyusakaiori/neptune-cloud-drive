package com.neptune.cloud.drive.server.context.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StoreShareFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 分享链接 ID
     */
    private long shareId;

    /**
     * 保存文件的 ID
     */
    private List<Long> shareFileIds;

    /**
     * 需要保存文件的目录 ID
     */
    private long targetId;

}
