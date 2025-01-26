package com.neptune.cloud.drive.server.request.file;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TransferUserFileRequest implements Serializable {

    private static final long serialVersionUID = -5761077034593506912L;

    /**
     * 移动的文件或者目录 ID
     */
    @ApiModelProperty("要转移的文件ID集合，多个使用公用分隔符隔开")
    @NotBlank(message = "转移的文件 ID 不能为空")
    private String sourceIds;

    /**
     * 目标的目录 ID
     */
    @ApiModelProperty("要转移到的目标文件夹的ID")
    @NotBlank(message = "转移的目标目录 ID 不能为空")
    private String targetId;
}
