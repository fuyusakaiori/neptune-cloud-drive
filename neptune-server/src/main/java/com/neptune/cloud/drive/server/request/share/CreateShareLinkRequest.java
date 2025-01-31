package com.neptune.cloud.drive.server.request.share;

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
public class CreateShareLinkRequest implements Serializable {

    private static final long serialVersionUID = -1377105134008918139L;

    /**
     * 分享的名称
     */
    @ApiModelProperty(value = "分享的名称", required = true)
    @NotBlank(message = "分享名称不能为空")
    private String shareName;

    /**
     * 分享的类型:
     */
    @ApiModelProperty(value = "分享的类型", required = true)
    @NotNull(message = "分享的类型不能为空")
    private Integer shareType;

    /**
     * 分享的过期时间
     */
    @ApiModelProperty(value = "分享的日期类型", required = true)
    @NotNull(message = "分享的日期类型不能为空")
    private Integer shareDayType;

    /**
     * 分享的文件
     */
    @ApiModelProperty(value = "分享的文件ID集合，多个使用公用的分割符去拼接", required = true)
    @NotBlank(message = "分享的文件ID不能为空")
    private String shareFileIds;



}
