package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 物理文件信息表
 */
@Data
@TableName(value ="cloud_drive_file")
public class File {
    /**
     * 文件id
     */
    @TableId(value = "file_id")
    private Long fileId;

    /**
     * 文件名称
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 文件物理路径
     */
    @TableField(value = "real_path")
    private String realPath;

    /**
     * 文件实际大小
     */
    @TableField(value = "file_size")
    private String fileSize;

    /**
     * 文件大小展示字符
     */
    @TableField(value = "file_size_desc")
    private String fileSizeDesc;

    /**
     * 文件后缀
     */
    @TableField(value = "file_suffix")
    private String fileSuffix;

    /**
     * 文件预览的响应头Content-Type的值
     */
    @TableField(value = "file_preview_content_type")
    private String filePreviewContentType;

    /**
     * 文件唯一标识
     */
    @TableField(value = "identifier")
    private String identifier;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", file_id=").append(fileId);
        sb.append(", filename=").append(filename);
        sb.append(", real_path=").append(realPath);
        sb.append(", file_size=").append(fileSize);
        sb.append(", file_size_desc=").append(fileSizeDesc);
        sb.append(", file_suffix=").append(fileSuffix);
        sb.append(", file_preview_content_type=").append(filePreviewContentType);
        sb.append(", identifier=").append(identifier);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}