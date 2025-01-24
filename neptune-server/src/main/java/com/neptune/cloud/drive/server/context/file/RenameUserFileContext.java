package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RenameUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件 ID
     */
    private long fileId;

    /**
     * 新的文件名称
     */
    private String newFilename;

}
