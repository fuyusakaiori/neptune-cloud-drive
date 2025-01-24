package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ListUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 目录 ID
     */
    private long parentId;

    /**
     * 文件类型
     */
    private List<Integer> fileTypes;



}
