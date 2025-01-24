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
public class DeleteUserFileRequest implements Serializable {

    private static final long serialVersionUID = -8574700254924198622L;

    /**
     * 文件 ID
     */
    @ApiModelProperty(value = "删除的文件 ID, 使用分割符进行分割")
    @NotBlank(message = "请选择需要删除的文件")
    private String fileIds;


}
