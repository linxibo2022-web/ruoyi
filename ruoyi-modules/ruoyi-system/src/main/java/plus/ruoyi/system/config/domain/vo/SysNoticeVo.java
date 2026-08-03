package plus.ruoyi.system.config.domain.vo;

import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.system.config.domain.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 通知公告视图对象 sys_notice
 *
 * @author Michelle.Chung
 */
@Data
@AutoMapper(target = SysNotice.class)
public class SysNoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
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
     * 公告内容（富文本中的图片URL会自动处理预签名）
     */
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String noticeContent;

    /**
     * 公告状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    @SerialMap(converter = SerialMapConstant.USER_ID_TO_NAME, source = "createBy")
    private String createByName;

    /**
     * 创建时间
     */
    private Date createTime;

    // ========== 扩展字段 ==========

    /**
     * 当前用户是否已读（前端使用）
     */
    private Boolean isRead;

    /**
     * 推送范围显示文本（前端使用）
     */
    private String targetText;

    /**
     * 已读人数（前端使用）
     */
    private Integer readCount;

    /**
     * 目标人数（前端使用）
     */
    private Integer targetCount;
}
