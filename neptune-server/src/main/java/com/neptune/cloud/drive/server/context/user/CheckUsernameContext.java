package com.neptune.cloud.drive.server.context.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckUsernameContext {

    /**
     * 用户账号
     */
    private String username;


}
