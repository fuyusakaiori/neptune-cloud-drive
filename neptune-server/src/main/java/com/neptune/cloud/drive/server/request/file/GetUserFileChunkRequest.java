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
public class GetUserFileChunkRequest implements Serializable {

    private static final long serialVersionUID = 172626217076356584L;

    /**
     * 文件唯一标识符
     */
    @ApiModelProperty(value = "文件唯一标识符")
    @NotBlank(message = "文件唯一标识符不可以为空")
    private String identifier;

}
