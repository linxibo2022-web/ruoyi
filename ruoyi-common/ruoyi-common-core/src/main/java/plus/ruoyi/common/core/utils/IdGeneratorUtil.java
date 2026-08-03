package plus.ruoyi.common.core.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ID生成工具类
 *
 * <p>提供多种ID生成策略，包括雪花ID、UUID等
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdGeneratorUtil {

    /**
     * 雪花算法实例
     */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    /**
     * 生成雪花ID
     *
     * @return 雪花ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成雪花ID字符串
     *
     * @return 雪花ID字符串
     */
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }

    /**
     * 生成简单UUID（不带-）
     *
     * @return UUID字符串
     */
    public static String simpleUUID() {
        return IdUtil.simpleUUID();
    }

    /**
     * 生成标准UUID
     *
     * @return UUID字符串
     */
    public static String randomUUID() {
        return IdUtil.randomUUID();
    }

    /**
     * 生成快速UUID（性能更好但随机性略低）
     *
     * @return UUID字符串
     */
    public static String fastUUID() {
        return IdUtil.fastUUID();
    }

    /**
     * 生成快速简单UUID（不带-，性能更好）
     *
     * @return UUID字符串
     */
    public static String fastSimpleUUID() {
        return IdUtil.fastSimpleUUID();
    }

}
