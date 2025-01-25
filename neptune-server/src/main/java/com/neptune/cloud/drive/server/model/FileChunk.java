package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件分片信息表
 */
@Data
@Accessors(chain = true)
@TableName(value ="cloud_drive_file_chunk")
public class FileChunk {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private long chunkId;

    /**
     * 文件唯一标识
     */
    @TableField(value = "identifier")
    private String identifier;

    /**
     * 分片真实的存储路径
     */
    @TableField(value = "real_path")
    private String realPath;

    /**
     * 分片编号
     */
    @TableField(value = "chunk_number")
    private long chunkNumber;

    /**
     * 过期时间
     */
    @TableField(value = "expiration_time")
    private Date expirationTime;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private long createUser;

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
        sb.append(", id=").append(chunkId);
        sb.append(", identifier=").append(identifier);
        sb.append(", real_path=").append(realPath);
        sb.append(", chunk_number=").append(chunkNumber);
        sb.append(", expiration_time=").append(expirationTime);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}