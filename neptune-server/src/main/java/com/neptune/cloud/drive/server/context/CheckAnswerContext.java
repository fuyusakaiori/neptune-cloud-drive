package com.neptune.cloud.drive.server.context;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckAnswerContext {

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户的密保问题
     */
    private String question;

    /**
     * 用户的密保答案
     */
    private String answer;

}
