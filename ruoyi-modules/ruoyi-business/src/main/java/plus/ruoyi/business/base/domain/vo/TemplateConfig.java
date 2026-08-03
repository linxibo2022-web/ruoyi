package plus.ruoyi.business.base.domain.vo;


import lombok.Data;
import plus.ruoyi.common.core.utils.DateUtils;

/**
 * 消息模板配置
 *
 * @author 抓蛙师
 * @date 2025/7/26
 */
@Data
public class TemplateConfig {

    /**
     * templateId
     */
    private String templateId;

    /**
     * 模板标题
     */
    private String title;

    /**
     * 模板内容/描述
     */
    private String content;

    /**
     * 字段列表 逗号隔开
     */
    private String fields;

    /**
     * 是否启用 1启用 0 禁用
     */
    private String status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 静态构造方法
     */
    public static TemplateConfig of(String templateId, String title, String content, String fields, String status, String remark) {
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setTemplateId(templateId);
        templateConfig.setTitle(title);
        templateConfig.setContent(content);
        templateConfig.setFields(fields);
        templateConfig.setStatus(status);
        String now = DateUtils.dateTimeNow();
        templateConfig.setCreateTime(now);
        templateConfig.setUpdateTime(now);
        templateConfig.setRemark(remark);
        return templateConfig;
    }
}
