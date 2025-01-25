package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetUserFileChunkContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件唯一标识符
     */
    private String identifier;
}
