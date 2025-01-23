package com.neptune.cloud.drive.server.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetUserRootDirContext {

    /**
     * 用户 ID
     */
    private long userId;

}
