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
public class RenameUserFileRequest implements Serializable {

    private static final long serialVersionUID = 1771063676941256616L;

    /**
     * 文件 ID
     */
    @ApiModelProperty(value = "文件 ID")
    @NotBlank(message = "文件 ID 不能为空")
    private String fileId;

    /**
     * 新的文件名称
     */
    @ApiModelProperty(value = "目录名称")
    @NotBlank(message = "新的文件名称不能为空")
    private String newFilename;

}
