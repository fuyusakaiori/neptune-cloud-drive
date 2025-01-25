package com.neptune.cloud.drive.server.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neptune.cloud.drive.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DirectoryTreeNodeVO implements Serializable {

    private static final long serialVersionUID = 3381673338330613119L;

    /**
     * 父目录 ID
     */
    @ApiModelProperty("父目录 ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long parentId;

    /**
     * 目录 ID
     */
    @ApiModelProperty("目录 ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long directoryId;

    /**
     * 目录名称
     */
    @ApiModelProperty("目录名称")
    private String directoryName;

    /**
     * 子目录
     */
    @ApiModelProperty("目录的子目录")
    private List<DirectoryTreeNodeVO> children;

}

