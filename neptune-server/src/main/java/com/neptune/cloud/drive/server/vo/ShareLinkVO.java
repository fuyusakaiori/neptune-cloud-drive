package com.neptune.cloud.drive.server.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neptune.cloud.drive.serializer.Date2StringSerializer;
import com.neptune.cloud.drive.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ShareLinkVO implements Serializable {

    private static final long serialVersionUID = -2508747981409977362L;

    @ApiModelProperty("分享的ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long shareId;

    @ApiModelProperty("分享的名称")
    private String shareName;

    @ApiModelProperty("分享的URL")
    private String shareUrl;

    @ApiModelProperty("分享的分享码")
    private String shareCode;

    @ApiModelProperty("分享的状态")
    private int shareStatus;

    @ApiModelProperty("分享的类型")
    private int shareType;

    @ApiModelProperty("分享的过期类型")
    private int shareDayType;

    @ApiModelProperty("分享的过期时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareExpireTime;

    @ApiModelProperty("分享的创建时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareCreateTime;

}
