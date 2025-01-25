package com.neptune.cloud.drive.server.request.file;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadUserFileChunkRequest implements Serializable {

    private static final long serialVersionUID = -7295871035663459578L;

    /**
     * 目录 ID
     */
    @ApiModelProperty(value = "目录 ID")
    @NotBlank(message = "目录 ID 不能为空")
    private String parentId;

    /**
     * 文件唯一标识符
     */
    @ApiModelProperty(value = "文件唯一标识符")
    @NotBlank(message = "文件唯一标识符不可以为空")
    private String identifier;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    @NotBlank(message = "文件名字不能为空")
    private String fileName;

    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小")
    @NotNull(message = "文件大小不可以为空")
    private Long fileSize;

    /**
     * 文件分片的编号
     */
    @ApiModelProperty(value = "文件分片的编号")
    @NotNull(message = "文件分片的编号不可以为空")
    private Long chunkSeq;

    /**
     * 文件分片的数量
     */
    @ApiModelProperty(value = "文件分片的数量")
    @NotNull(message = "文件分片的数量不可以为空")
    private Long chunkCount;

    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件分片的大小")
    @NotNull(message = "文件分片的大小不可以为空")
    private Long chunkSize;


    /**
     * 文件实体
     */
    @ApiModelProperty(value = "文件分片")
    @NotNull(message = "文件分片不可以为空")
    private MultipartFile chunk;

}
