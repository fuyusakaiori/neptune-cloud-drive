package com.neptune.cloud.drive.server.context.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetShareFileInfoContext {

    /**
     * 分享链接 ID
     */
    private long shareId;

}
