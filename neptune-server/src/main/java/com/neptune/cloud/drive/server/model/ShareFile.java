package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户分享文件表
 */
@Data
@Accessors(chain = true)
@TableName(value ="cloud_drive_share_file")
public class ShareFile {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long sharFileId;

    /**
     * 分享id
     */
    @TableField(value = "share_id")
    private Long shareId;

    /**
     * 文件记录ID
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * 分享创建人
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
        sb.append(", id=").append(sharFileId);
        sb.append(", share_id=").append(shareId);
        sb.append(", file_id=").append(fileId);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}