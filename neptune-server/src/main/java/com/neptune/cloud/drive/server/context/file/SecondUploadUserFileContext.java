package com.neptune.cloud.drive.server.context.file;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SecondUploadUserFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 目录 ID
     */
    private long parentId;

    /**
     * 文件唯一标识符
     */
    private String identifier;

    /**
     * 文件名称
     */
    private String filename;

}
