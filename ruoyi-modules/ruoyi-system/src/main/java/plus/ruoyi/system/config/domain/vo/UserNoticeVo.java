package plus.ruoyi.system.config.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.system.config.domain.SysNotice;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户通知公告视图对象
 *
 * @author Lion Li
 */
@Data
@AutoMapper(target = SysNotice.class, convertGenerate = false)
public class UserNoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    @SerialMap(source = "createBy", converter = SerialMapConstant.USER_ID_TO_NAME)
    private String createByName;


    /**
     * 当前用户是否已读
     */
    private Boolean isRead;

}
