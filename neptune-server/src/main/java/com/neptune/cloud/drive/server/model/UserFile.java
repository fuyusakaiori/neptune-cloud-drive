package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户文件信息表
 */
@TableName(value ="cloud_drive_user_file")
@Data
public class UserFile {
    /**
     * 文件记录ID
     */
    @TableId(value = "file_id")
    private Long fileId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 上级文件夹ID,顶级文件夹为0
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 真实文件id
     */
    @TableField(value = "real_file_id")
    private Long realFileId;

    /**
     * 文件名
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 是否是文件夹 （0 否 1 是）
     */
    @TableField(value = "folder_flag")
    private Integer folderFlag;

    /**
     * 文件大小展示字符
     */
    @TableField(value = "file_size_desc")
    private String fileSizeDesc;

    /**
     * 文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 删除标识（0 否 1 是）
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

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

    /**
     * 更新人
     */
    @TableField(value = "update_user")
    private Long updateUser;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", file_id=").append(fileId);
        sb.append(", user_id=").append(userId);
        sb.append(", parent_id=").append(parentId);
        sb.append(", real_file_id=").append(realFileId);
        sb.append(", filename=").append(filename);
        sb.append(", folder_flag=").append(folderFlag);
        sb.append(", file_size_desc=").append(fileSizeDesc);
        sb.append(", file_type=").append(fileType);
        sb.append(", del_flag=").append(delFlag);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append(", update_user=").append(updateUser);
        sb.append(", update_time=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}