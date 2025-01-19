package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 错误日志表
 */
@Data
@TableName(value ="cloud_drive_error_log")
public class ErrorLog {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long logId;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    private String logContent;

    /**
     * 日志状态：0 未处理 1 已处理
     */
    @TableField(value = "log_status")
    private Integer logStatus;

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
        sb.append(", id=").append(logId);
        sb.append(", log_content=").append(logContent);
        sb.append(", log_status=").append(logStatus);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append(", update_user=").append(updateUser);
        sb.append(", update_time=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}