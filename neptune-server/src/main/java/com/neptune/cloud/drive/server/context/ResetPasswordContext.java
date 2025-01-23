package com.neptune.cloud.drive.server.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResetPasswordContext {

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户新密码
     */
    private String password;

    /**
     * 用户重设密码的临时 token
     */
    private String token;

}
