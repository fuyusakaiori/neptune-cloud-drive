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
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = -2936499854730811364L;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    private String username;

    /**
     * 用户根目录 ID: 使用序列化器转换字符串并加密, 不仅可以确保安全, 同时防止精度丢失
     */
    @ApiModelProperty(value = "用户根目录 ID", required = true)
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long rootDirId;

    /**
     * 用户根目录名称
     */
    @ApiModelProperty(value = "用户根目录名称", required = true)
    private String rootDirName;


}
