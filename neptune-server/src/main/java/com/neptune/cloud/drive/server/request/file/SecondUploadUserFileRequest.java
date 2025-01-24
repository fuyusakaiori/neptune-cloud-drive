package com.neptune.cloud.drive.server.request.file;

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
public class SecondUploadUserFileRequest implements Serializable {

    private static final long serialVersionUID = 5345231326798990788L;

    /**
     * 目录 ID
     */
    @ApiModelProperty(value = "目录 ID", required = true)
    @NotBlank(message = "目录 ID 不可以为空")
    private String parentId;

    /**
     * 文件唯一标识符
     */
    @ApiModelProperty(value = "秒传文件唯一标识符", required = true)
    @NotBlank(message = "秒传文件的唯一标识不能为空")
    private String identifier;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "秒传文件名称", required = true)
    @NotBlank(message = "秒传文件名称不能为空")
    private String filename;


}
