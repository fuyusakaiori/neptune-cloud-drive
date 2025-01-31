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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ShareFileInfoVO implements Serializable {

    private static final long serialVersionUID = -2446579294335071804L;

    @JsonSerialize(using = IdEncryptSerializer.class)
    @ApiModelProperty("分享 ID")
    private long shareId;

    @ApiModelProperty("分享的名称")
    private String shareName;

    @ApiModelProperty("分享的过期类型")
    private int shareDay;

    @ApiModelProperty("分享的创建时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareCreateTime;

    @ApiModelProperty("分享的过期时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareExpireTime;

    @ApiModelProperty("分享者的信息")
    private ShareUserVO shareUser;

    @ApiModelProperty("分享的文件列表")
    private List<ShareFileVO> shareFiles;
}
