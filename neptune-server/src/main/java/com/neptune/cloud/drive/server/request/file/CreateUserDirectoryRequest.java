package com.neptune.cloud.drive.server.request.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(value = "创建目录参数实体")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateUserDirectoryRequest implements Serializable {

    private static final long serialVersionUID = -5205626312898710288L;

    /**
     * 父目录 ID
     */
    @ApiModelProperty(value = "父目录 ID")
    @NotBlank(message = "父目录 ID 不能为空")
    private String parentId;

    /**
     * 目录名称
     */
    @ApiModelProperty(value = "目录名称")
    @NotBlank(message = "目录名称不能为空")
    private String directoryName;

}
