package com.neptune.cloud.drive.server.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neptune.cloud.drive.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateShareLinkVO implements Serializable {

    @ApiModelProperty("分享链接的ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long shareId;

    @ApiModelProperty("分享链接的名称")
    private String shareName;

    @ApiModelProperty("分享链接的URL")
    private String shareUrl;

    @ApiModelProperty("分享链接的分享码")
    private String shareCode;

    @ApiModelProperty("分享链接的状态")
    private int shareStatus;

}
