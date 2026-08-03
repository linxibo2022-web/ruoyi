package plus.ruoyi.common.serialmap.constant;

import plus.ruoyi.common.serialmap.annotation.SerialMap;

/**
 * 序列化映射转换器类型常量
 *
 * <p>定义系统内置的序列化映射转换器类型标识，用于{@link SerialMap}注解的converter参数。
 *
 * <p>使用场景：
 * <ul>
 *   <li>在VO类字段上使用，避免硬编码转换器类型</li>
 *   <li>为转换器实现类提供统一的类型标识</li>
 *   <li>便于IDE代码提示和重构</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * public class UserVo {
 *     // 使用常量而非硬编码字符串
 *     {@code @SerialMap(converter = SerialMapConstant.USER_ID_TO_NAME)}
 *     private Long userId;
 *
 *     {@code @SerialMap(converter = SerialMapConstant.DEPT_ID_TO_NAME)}
 *     private Long deptId;
 * }
 * </pre>
 *
 * @author Lion Li
 */
public interface SerialMapConstant {

    /**
     * 国际化翻译转换器 - 基于MessageSource的多语言翻译
     */
    String I18N_TRANSLATE = "i18n_translate";

    /**
     * 通用字段映射转换器 - 基于实体类的字段映射
     */
    String FIELD_MAP = "field_map";

    /**
     * 用户ID转账号转换器 - 将用户ID转换为用户登录账号
     */
    String USER_ID_TO_NAME = "user_id_to_name";

    /**
     * 用户ID转昵称转换器 - 将用户ID转换为用户显示昵称
     */
    String USER_ID_TO_NICKNAME = "user_id_to_nickname";

    /**
     * 用户ID转昵称转换器 - 将用户ID转换为用户显示头像
     */
    String USER_ID_TO_AVATAR = "user_id_to_avatar";

    /**
     * 部门ID转名称转换器 - 将部门ID转换为部门名称
     */
    String DEPT_ID_TO_NAME = "dept_id_to_name";

    /**
     * 字典转换器 - 将字典类型值转换为字典标签
     */
    String DICT_TYPE_TO_LABEL = "dict_type_to_label";

    /**
     * OSS文件ID转URL转换器 - 将文件存储ID转换为访问URL
     */
    String OSS_ID_TO_URL = "oss_id_to_url";

    /**
     * 目录ID转目录名称转换器 - 将目录ID转换为目录名称
     */
    String DIRECTORY_ID_DIRECTORY_NAME = "directory_id_directory_name";

    /**
     * 预签名URL转换器 - 为私有文件URL生成预签名访问URL
     * <p>支持单个URL和多URL（逗号分隔）处理
     * <p>使用示例：
     * <pre>
     * // 基础用法：使用默认配置（检测/private/前缀，60分钟有效期）
     * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)}
     * private String imageUrl;
     *
     * // 指定源字段：从其他字段获取URL路径
     * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL, source = "filePath")}
     * private String fileUrl;
     *
     * // 多URL处理：逗号分隔的URL字符串
     * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)}
     * private String galleryUrls; // 如："/private/1.jpg,/private/2.jpg"
     * </pre>
     */
    String PRESIGNED_URL = "presigned_url";

}
