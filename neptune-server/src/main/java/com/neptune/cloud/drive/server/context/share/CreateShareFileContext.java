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
public class CreateShareFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 分享 ID
     */
    private long shareId;

    /**
     * 分享文件 ID
     */
    private List<Long> shareFileIds;

}
