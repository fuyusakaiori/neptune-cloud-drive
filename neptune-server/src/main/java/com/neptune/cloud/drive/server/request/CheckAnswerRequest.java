package com.neptune.cloud.drive.server.request;

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
public class CheckAnswerRequest implements Serializable {

    private static final long serialVersionUID = -3287559443797299499L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    @NotBlank(message = "用户账号不可以为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,16}$", message = "请输入 6-16 位的只包含数字和大小写字母的用户账号")
    private String username;

    /**
     * 用户的密保问题
     */
    @ApiModelProperty(value = "密保问题", required = true)
    @NotBlank(message = "用户账号的密保问题不可以为空")
    @Length(max = 100, message = "密保问题不可以超过 100 个字符")
    private String question;

    /**
     * 用户的密保答案
     */
    @ApiModelProperty(value = "密保答案", required = true)
    @NotBlank(message = "用户账号的密保答案不可以为空")
    @Length(max = 100, message = "密保问题答案不可以超过 100 个字符")
    private String answer;

}
