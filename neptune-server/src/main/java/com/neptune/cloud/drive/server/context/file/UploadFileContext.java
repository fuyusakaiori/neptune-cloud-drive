package com.neptune.cloud.drive.server.context.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadFileContext {

    /**
     * 用户 ID
     */
    private long userId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件唯一标识符
     */
    private String identifier;

    /**
     * 文件
     */
    private MultipartFile file;

}
