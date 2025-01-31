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
public class ShareLinkInfoVO implements Serializable {

    private static final long serialVersionUID = -7440927419397552327L;

    @ApiModelProperty("分享链接")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long shareId;

    @ApiModelProperty("分享链接名称")
    private String shareName;

    @ApiModelProperty("分享人信息")
    private ShareUserVO shareUser;

}
