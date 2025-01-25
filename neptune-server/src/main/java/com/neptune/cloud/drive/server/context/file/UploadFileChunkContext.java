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
public class UploadFileChunkContext {

    /**
     * 用户 ID
     */
    private long userId;


    /**
     * 文件唯一标识符
     */
    private String identifier;

    /**
     * 文件名称
     */
    private String fileName;


    /**
     * 文件分片的编号
     */
    private long chunkSeq;

    /**
     * 文件分片的数量
     */
    private long chunkCount;

    /**
     * 文件大小
     */
    private long chunkSize;


    /**
     * 文件实体
     */
    private MultipartFile chunk;

}
