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
public class CancelShareLinkRequest implements Serializable {

    private static final long serialVersionUID = 1548571927331841095L;

    /**
     * 分享链接 ID
     */
    @ApiModelProperty(value = "取消的分享链接 ID", required = true)
    @NotBlank(message = "取消的分享链接 ID 不能为空")
    private String shareIds;

}
