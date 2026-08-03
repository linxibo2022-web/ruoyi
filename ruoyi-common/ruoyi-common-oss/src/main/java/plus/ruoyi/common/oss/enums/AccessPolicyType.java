package plus.ruoyi.common.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.ruoyi.common.oss.exception.OssException;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

/**
 * 桶访问策略配置
 *
 * @author 陈賝
 */
@Getter
@AllArgsConstructor
public enum AccessPolicyType {

    /**
     * private - 私有
     */
    PRIVATE("0", BucketCannedACL.PRIVATE, ObjectCannedACL.PRIVATE),

    /**
     * public - 公共读写
     */
    PUBLIC("1", BucketCannedACL.PUBLIC_READ_WRITE, ObjectCannedACL.PUBLIC_READ_WRITE),

    /**
     * custom - 自定义（公共读）
     */
    CUSTOM("2", BucketCannedACL.PUBLIC_READ, ObjectCannedACL.PUBLIC_READ);

    /**
     * 桶 权限类型（数据库值）
     */
    private final String type;

    /**
     * 桶 权限类型
     */
    private final BucketCannedACL bucketCannedACL;

    /**
     * 文件对象 权限类型
     */
    private final ObjectCannedACL objectCannedACL;

    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return 枚举
     */
    public static AccessPolicyType getByType(String type) {
        for (AccessPolicyType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        throw new OssException("'type' not found By " + type);
    }
}
