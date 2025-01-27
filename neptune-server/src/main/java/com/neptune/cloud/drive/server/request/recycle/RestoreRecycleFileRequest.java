package com.neptune.cloud.drive.server.request.recycle;

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
public class RestoreRecycleFileRequest implements Serializable {

    private static final long serialVersionUID = -2308188839386710655L;

    /**
     * 需要还原的文件或者目录的 ID
     */
    @ApiModelProperty(value = "需要还原的文件 ID")
    @NotBlank(message = "需要还原的文件 ID 不能为空")
    private String fileIds;

}
