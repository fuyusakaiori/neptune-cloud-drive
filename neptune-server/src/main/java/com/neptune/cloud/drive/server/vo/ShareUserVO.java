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
public class ShareUserVO implements Serializable {

    private static final long serialVersionUID = -1002613882288072203L;

    @ApiModelProperty("分享者 ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private long userId;

    @ApiModelProperty("分享者的名称")
    private String username;

}
