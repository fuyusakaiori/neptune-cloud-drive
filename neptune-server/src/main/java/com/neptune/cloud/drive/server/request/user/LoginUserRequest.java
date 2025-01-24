package com.neptune.cloud.drive.server.request.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@ApiModel(value = "用户登录参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LoginUserRequest implements Serializable {

    private static final long serialVersionUID = -5134290437817623141L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    @NotBlank(message = "用户账号不可以为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,16}$", message = "请输入 6-16 位的只包含数字和大小写字母的用户账号")
    private String username;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码", required = true)
    @NotBlank(message = "用户密码不可以为空")
    @Length(min = 8, max = 16, message = "请输入 8-16 位的密码")
    private String password;

}
