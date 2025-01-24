package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DeleteUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 删除的文件 ID
     */
    private List<Long> fileIdList;

}
