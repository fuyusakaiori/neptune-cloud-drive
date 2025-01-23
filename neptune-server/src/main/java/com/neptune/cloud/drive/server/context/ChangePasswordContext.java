package com.neptune.cloud.drive.server.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChangePasswordContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 用户旧密码
     */
    private String oldPassword;

    /**
     * 用户新密码
     */
    private String newPassword;


}
