package plus.ruoyi.common.oss.factory;

import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.oss.enums.OssType;
import plus.ruoyi.common.oss.service.impl.LocalOssStrategy;
import plus.ruoyi.common.oss.service.OssStrategy;
import plus.ruoyi.common.oss.service.impl.S3OssStrategy;
import plus.ruoyi.common.oss.entity.OssClientConfig;

/**
 * OSS策略工厂类
 * 用于创建具体的OSS存储策略实现
 *
 * @author 抓蛙师
 */
public class OssStrategyFactory {

    /**
     * 根据配置键创建存储策略
     *
     * @param configKey     配置键
     * @param ossClientConfig OSS 客户端配置
     * @return 存储策略
     */
    public static OssStrategy createStrategy(String configKey, OssClientConfig ossClientConfig) {
        if (Constants.LOCAL.equals(configKey)) {
            return new LocalOssStrategy(configKey, ossClientConfig);
        }
        return new S3OssStrategy(configKey, ossClientConfig);
    }

    /**
     * 根据OSS类型创建存储策略
     *
     * @param ossType       OSS类型
     * @param ossClientConfig OSS 客户端配置
     * @return 存储策略
     */
    public static OssStrategy createStrategy(OssType ossType, OssClientConfig ossClientConfig) {
        return createStrategy(ossType.getValue(), ossClientConfig);
    }
}
