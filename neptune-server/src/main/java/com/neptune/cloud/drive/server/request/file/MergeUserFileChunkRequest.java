package com.neptune.cloud.drive.server.request.file;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MergeUserFileChunkRequest implements Serializable {

    private static final long serialVersionUID = -8584803561589580698L;

    /**
     * 目录 ID
     */
    @ApiModelProperty(value = "目录 ID", required = true)
    @NotBlank(message = "目录 ID 不可以为空")
    private String parentId;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称", required = true)
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    /**
     * 文件唯一标识符
     */
    @ApiModelProperty(value = "文件唯一标识", required = true)
    @NotBlank(message = "文件唯一标识不能为空")
    private String identifier;

    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小", required = true)
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;


}
