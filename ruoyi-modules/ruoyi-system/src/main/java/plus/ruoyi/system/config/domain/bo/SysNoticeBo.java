package plus.ruoyi.system.config.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.xss.Xss;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.system.config.domain.SysNotice;

import java.util.List;

/**
 * 通知公告业务对象 sys_notice
 *
 * @author Michelle.Chung
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysNotice.class, reverseConvertGenerate = false)
public class SysNoticeBo extends BaseEntity {

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告类型（1通知 2公告）
     */
    @NotBlank(message = "公告类型不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过{max}个字符")
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

    /**
     * 创建人名称
     */
    private String createByName;

    // ========== 推送相关字段 ==========

    /**
     * 推送类型：all-全员 dept-部门 role-角色 user-指定用户
     */
    private String pushType;

    /**
     * 选中的部门ID列表
    /**
     * 选中的部门ID列表
     */
    private List<Long> deptIds;

    /**
     * 选中的角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 选中的用户ID列表
     */
    private List<Long> userIds;
}
