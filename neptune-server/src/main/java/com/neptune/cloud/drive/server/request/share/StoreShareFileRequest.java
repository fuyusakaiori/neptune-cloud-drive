package com.neptune.cloud.drive.server.request.share;

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
public class StoreShareFileRequest implements Serializable {

    /**
     * 保存文件的 ID
     */
    @ApiModelProperty(value = "保存的文件 ID", required = true)
    @NotBlank(message = "保存的文件 ID 不能为空")
    private String shareFileIds;

    /**
     * 需要保存文件的目录 ID
     */
    @ApiModelProperty(value = "要转存到的文件夹ID", required = true)
    @NotBlank(message = "保存文件的目录 ID")
    private String targetId;

}
