package com.neptune.cloud.drive.server.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neptune.cloud.drive.serializer.IdEncryptSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@ApiModel(value = "文件列表实体")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserFileVO implements Serializable {

    private static final long serialVersionUID = 8802247209739236288L;

    /**
     * 目录 ID
     */
    @ApiModelProperty(value = "目录 ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long parentId;

    /**
     * 文件 ID
     */
    @ApiModelProperty(value = "文件 ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long fileId;

    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型")
    private int fileType;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    private String fileName;

    /**
     * 文件描述
     */
    @ApiModelProperty(value = "文件描述")
    private String description;

    /**
     * 是否为目录
     */
    @ApiModelProperty(value = "是否为目录")
    private boolean directory;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
