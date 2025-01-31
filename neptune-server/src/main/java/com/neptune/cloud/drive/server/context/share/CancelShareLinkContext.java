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
public class CancelShareLinkContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 取消的分享 ID 集合
     */
    private List<Long> shareIds;

}
