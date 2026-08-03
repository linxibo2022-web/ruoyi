package plus.ruoyi.system.config.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import plus.ruoyi.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 通知公告表 sys_notice
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends TenantEntity {

    /**
     * 公告ID
     */
    @TableId(value = "notice_id")
    private Long noticeId;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 推送配置JSON
     */
    private String targetConfig;

    /**
     * 目标用户ID列表
     */
    private String targetUserIds;

    /**
     * 已读用户ID列表
     */
    private String readUserIds;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

}
