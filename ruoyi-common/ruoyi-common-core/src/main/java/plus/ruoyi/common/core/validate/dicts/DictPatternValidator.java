package plus.ruoyi.common.core.validate.dicts;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 字典值校验器
 * <p>实现DictPattern注解的校验逻辑，验证字段值是否为指定字典类型中的有效值</p>
 * <p>支持单个值校验和多个值（分隔符分割）的批量校验</p>
 *
 * @author AprilWind
 */
@Slf4j
public class DictPatternValidator implements ConstraintValidator<DictPattern, String> {

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 分隔符
     */
    private String separator;

    /**
     * 是否允许空值
     */
    private boolean allowEmpty;

    /**
     * 字典服务实例（延迟获取）
     */
    private DictService dictService;

    /**
     * 初始化校验器
     * <p>从注解中提取配置参数</p>
     *
     * @param annotation DictPattern注解实例
     */
    @Override
    public void initialize(DictPattern annotation) {
        this.dictType = annotation.dictType();
        this.separator = annotation.separator();
        this.allowEmpty = annotation.allowEmpty();

        log.debug("初始化字典校验器: dictType={}, separator={}, allowEmpty={}",
            dictType, separator, allowEmpty);
    }

    /**
     * 执行字典值校验
     * <p>校验逻辑：</p>
     * <ol>
     *     <li>空值检查：根据allowEmpty配置决定是否允许空值</li>
     *     <li>委托给字典服务进行校验（支持单值和多值）</li>
     *     <li>通过返回的标签判断所有值是否都有效</li>
     * </ol>
     *
     * @param value   待校验的字段值（单个值或多个值用分隔符分割）
     * @param context 校验上下文，用于构建自定义错误信息
     * @return true表示校验通过，false表示校验失败
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 空值检查
        if (StringUtils.isBlank(value)) {
            return allowEmpty;
        }

        // 字典类型检查
        if (StringUtils.isBlank(dictType)) {
            log.warn("字典类型不能为空");
            return false;
        }

        try {
            // 延迟获取字典服务实例
            if (dictService == null) {
                dictService = SpringUtils.getBean(DictService.class);
            }

            // 调用字典服务获取标签（内部已处理单值和多值情况）
            String dictLabel = dictService.getDictLabel(dictType, value, separator);

            // 检查是否所有值都有效
            if (StringUtils.isBlank(dictLabel)) {
                log.debug("字典值校验失败: dictType={}, value={}", dictType, value);
                buildCustomErrorMessage(context, value);
                return false;
            }

            // 对于多值情况，检查返回的标签中是否包含空字符串（表示某个值无效）
            if (value.contains(separator)) {
                String[] labels = dictLabel.split(separator);
                for (String label : labels) {
                    if (StringUtils.isBlank(label.trim())) {
                        log.debug("字典值校验失败，包含无效值: dictType={}, value={}", dictType, value);
                        buildCustomErrorMessage(context, value);
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            log.error("字典值校验异常: dictType={}, value={}", dictType, value, e);
            return false;
        }
    }

    /**
     * 构建自定义错误消息
     *
     * @param context      校验上下文
     * @param invalidValue 无效的字典值
     */
    private void buildCustomErrorMessage(ConstraintValidatorContext context, String invalidValue) {
        // 禁用默认错误消息
        context.disableDefaultConstraintViolation();

        // 构建包含具体无效值的错误消息
        String customMessage = String.format("字典值[%s]无效，不在[%s]字典范围内", invalidValue, dictType);
        context.buildConstraintViolationWithTemplate(customMessage)
            .addConstraintViolation();
    }

}
