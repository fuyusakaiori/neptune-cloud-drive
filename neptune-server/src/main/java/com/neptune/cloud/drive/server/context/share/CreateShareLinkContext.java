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
public class CreateShareLinkContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 分享的名称
     */
    private String shareName;

    /**
     * 分享的类型
     */
    private int shareType;

    /**
     * 分享的日期类型
     */
    private int shareDayType;

    /**
     * 该分项对应的文件ID集合
     */
    private List<Long> shareFileIds;

}
