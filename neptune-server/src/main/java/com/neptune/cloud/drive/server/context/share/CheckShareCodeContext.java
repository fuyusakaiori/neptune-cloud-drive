package com.neptune.cloud.drive.server.context.share;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckShareCodeContext {

    /**
     * 分享链接 ID
     */
    private String shareId;

    /**
     * 分享链接的校验码
     */
    private String shareCode;

}
