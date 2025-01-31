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
public class CheckShareCodeRequest implements Serializable {

    private static final long serialVersionUID = 2707744223215090869L;

    @ApiModelProperty(value = "分享链接的 ID", required = true)
    @NotBlank(message = "分享链接 ID 不能为空")
    private String shareId;

    @ApiModelProperty(value = "分享链接的校验码", required = true)
    @NotBlank(message = "分享链接的校验码不能为空")
    private String shareCode;

}
