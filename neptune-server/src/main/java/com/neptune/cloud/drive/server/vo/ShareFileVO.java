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
public class ShareFileVO implements Serializable {

    private static final long serialVersionUID = -3281751172196166322L;

    @ApiModelProperty(value = "文件ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long fileId;

    @ApiModelProperty(value = "父文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long parentId;

    @ApiModelProperty(value = "文件名称")
    private String filename;

    @ApiModelProperty(value = "文件大小描述")
    private String fileSizeDesc;

    @ApiModelProperty(value = "文件夹标识 0 否 1 是")
    private Integer folderFlag;

    @ApiModelProperty(value = "文件类型 1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv")
    private Integer fileType;

    @ApiModelProperty(value = "文件更新时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date updateTime;

}
