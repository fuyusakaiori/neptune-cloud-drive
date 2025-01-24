package com.neptune.cloud.drive.server.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 3992645752639976371L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    @NotBlank(message = "用户账号不可以为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,16}$", message = "请输入 6-16 位的只包含数字和大小写字母的用户账号")
    private String username;

    /**
     * 用户旧密码
     */
    @ApiModelProperty(value = "用户旧密码", required = true)
    @NotBlank(message = "用户旧密码不可以为空")
    @Length(min = 8, max = 16, message = "请输入 8-16 位的旧密码")
    private String oldPassword;

    /**
     * 用户新密码
     */
    @ApiModelProperty(value = "用户新密码", required = true)
    @NotBlank(message = "用户新密码不可以为空")
    @Length(min = 8, max = 16, message = "请输入 8-16 位的新密码")
    private String newPassword;

}
