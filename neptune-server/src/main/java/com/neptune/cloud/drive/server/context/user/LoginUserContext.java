package com.neptune.cloud.drive.server.context.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LoginUserContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

}
