package com.neptune.cloud.drive.server.context.recycle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RestoreRecycleFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件 ID
     */
    private List<Long> fileIds;

}
