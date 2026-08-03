package plus.ruoyi.common.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OSS类型枚举
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum OssType {

    /**
     * 本地存储
     */
    LOCAL("local"),

    /**
     * 阿里云OSS
     */
    ALIYUN("aliyun"),

    /**
     * 腾讯云COS
     */
    QCLOUD("qcloud"),

    /**
     * 七牛云
     */
    QINIU("qiniu"),

    /**
     * MinIO
     */
    MINIO("minio"),

    /**
     * 华为云OBS
     */
    OBS("obs");

    /**
     * 值
     */
    private final String value;

    /**
     * 根据值获取类型
     *
     * @param value 值
     * @return 类型
     */
    public static OssType getByValue(String value) {
        for (OssType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的OSS类型: " + value);
    }
}
