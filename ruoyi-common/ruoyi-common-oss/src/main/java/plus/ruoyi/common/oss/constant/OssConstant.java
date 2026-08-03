package plus.ruoyi.common.oss.constant;

import plus.ruoyi.common.core.constant.GlobalConstants;

import java.util.List;

/**
 * 对象存储常量
 *
 * @author Lion Li
 */
public interface OssConstant {

    /**
     * 全部目录
     */
    Long ALL = 9999999999999999L;

    /**未分类目录*/
    Long UNCATEGORIZED = 10000000000000000L;

    /**
     * 上传接口路径
     */
    String UPLOAD_PATH = "/resource/oss/upload";

    /**
     * 资源完整访问路径前缀（带斜杠）
     */
    String RESOURCE_PATH = "resources";

    /**
     * 资源完整访问路径前缀（带斜杠）
     */
    String RESOURCE_PREFIX = "/" + RESOURCE_PATH;

    /**
     * 默认配置KEY
     */
    String DEFAULT_CONFIG_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "sys_oss:default_config";

    /**
     * 预览列表资源开关Key
     */
    String PREVIEW_LIST_RESOURCE_KEY = "system.oss.preview-enabled";

    /**
     * 系统数据ids
     */
    List<Long> SYSTEM_DATA_IDS = List.of(1L, 2L, 3L, 4L);

    /**
     * 云服务商
     */
    String[] CLOUD_SERVICE = new String[]{"aliyun", "qcloud", "qiniu", "obs"};

    /**
     * https 状态
     */
    String IS_HTTPS = "1";

}
