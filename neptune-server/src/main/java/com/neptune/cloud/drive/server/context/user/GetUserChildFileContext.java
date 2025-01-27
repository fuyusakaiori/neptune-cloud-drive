package com.neptune.cloud.drive.server.context.user;

import com.neptune.cloud.drive.server.model.UserFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetUserChildFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 需要查询子文件的目录
     */
    private List<UserFile> files;

}
