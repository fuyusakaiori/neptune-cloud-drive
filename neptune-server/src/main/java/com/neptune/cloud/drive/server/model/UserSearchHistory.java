package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户搜索历史表
 */
@TableName(value ="cloud_drive_user_search_history")
@Data
public class UserSearchHistory {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long historyId;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 搜索文案
     */
    @TableField(value = "search_content")
    private String searchContent;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

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
        sb.append(", id=").append(historyId);
        sb.append(", user_id=").append(userId);
        sb.append(", search_content=").append(searchContent);
        sb.append(", create_time=").append(create_time);
        sb.append(", update_time=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}