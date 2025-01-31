package com.neptune.cloud.drive.server.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户分享表
 */
@Data
@Accessors(chain = true)
@TableName(value ="cloud_drive_share")
public class Share {
    /**
     * 分享id
     */
    @TableId(value = "share_id")
    private Long shareId;

    /**
     * 分享名称
     */
    @TableField(value = "share_name")
    private String shareName;

    /**
     * 分享类型（0 有提取码）
     */
    @TableField(value = "share_type")
    private Integer shareType;

    /**
     * 分享类型（0 永久有效；1 7天有效；2 30天有效）
     */
    @TableField(value = "share_day_type")
    private Integer shareDayType;

    /**
     * 分享有效天数（永久有效为0）
     */
    @TableField(value = "share_day")
    private Integer shareDay;

    /**
     * 分享结束时间
     */
    @TableField(value = "share_end_time")
    private Date shareEndTime;

    /**
     * 分享链接地址
     */
    @TableField(value = "share_url")
    private String shareUrl;

    /**
     * 分享提取码
     */
    @TableField(value = "share_code")
    private String shareCode;

    /**
     * 分享状态（0 正常；1 有文件被删除）
     */
    @TableField(value = "share_status")
    private Integer shareStatus;

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
        sb.append(", share_id=").append(shareId);
        sb.append(", share_name=").append(shareName);
        sb.append(", share_type=").append(shareType);
        sb.append(", share_day_type=").append(shareDayType);
        sb.append(", share_day=").append(shareDay);
        sb.append(", share_end_time=").append(shareEndTime);
        sb.append(", share_url=").append(shareUrl);
        sb.append(", share_code=").append(shareCode);
        sb.append(", share_status=").append(shareStatus);
        sb.append(", create_user=").append(createUser);
        sb.append(", create_time=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}