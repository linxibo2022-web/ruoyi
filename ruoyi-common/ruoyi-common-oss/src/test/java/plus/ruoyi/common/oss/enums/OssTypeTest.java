package plus.ruoyi.common.oss.enums;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssType OSS类型枚举测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssType OSS类型枚举测试")
public class OssTypeTest extends BaseUnitTest {

    // ==================== 枚举值测试 ====================

    @Test
    @DisplayName("测试枚举值-LOCAL本地存储")
    public void testLocalType() {
        OssType type = OssType.LOCAL;

        assertEquals("local", type.getValue(), "LOCAL类型值应为local");
        assertEquals("LOCAL", type.name(), "枚举名称应为LOCAL");
    }

    @Test
    @DisplayName("测试枚举值-ALIYUN阿里云")
    public void testAliyunType() {
        OssType type = OssType.ALIYUN;

        assertEquals("aliyun", type.getValue(), "ALIYUN类型值应为aliyun");
        assertEquals("ALIYUN", type.name(), "枚举名称应为ALIYUN");
    }

    @Test
    @DisplayName("测试枚举值-QCLOUD腾讯云")
    public void testQcloudType() {
        OssType type = OssType.QCLOUD;

        assertEquals("qcloud", type.getValue(), "QCLOUD类型值应为qcloud");
        assertEquals("QCLOUD", type.name(), "枚举名称应为QCLOUD");
    }

    @Test
    @DisplayName("测试枚举值-QINIU七牛云")
    public void testQiniuType() {
        OssType type = OssType.QINIU;

        assertEquals("qiniu", type.getValue(), "QINIU类型值应为qiniu");
        assertEquals("QINIU", type.name(), "枚举名称应为QINIU");
    }

    @Test
    @DisplayName("测试枚举值-MINIO")
    public void testMinioType() {
        OssType type = OssType.MINIO;

        assertEquals("minio", type.getValue(), "MINIO类型值应为minio");
        assertEquals("MINIO", type.name(), "枚举名称应为MINIO");
    }

    @Test
    @DisplayName("测试枚举值-OBS华为云")
    public void testObsType() {
        OssType type = OssType.OBS;

        assertEquals("obs", type.getValue(), "OBS类型值应为obs");
        assertEquals("OBS", type.name(), "枚举名称应为OBS");
    }

    // ==================== values()测试 ====================

    @Test
    @DisplayName("测试values-枚举数量")
    public void testValuesCount() {
        OssType[] types = OssType.values();

        assertEquals(6, types.length, "应有6种OSS类型");
    }

    @Test
    @DisplayName("测试values-包含所有类型")
    public void testValuesContainsAll() {
        OssType[] types = OssType.values();

        boolean hasLocal = false;
        boolean hasAliyun = false;
        boolean hasQcloud = false;
        boolean hasQiniu = false;
        boolean hasMinio = false;
        boolean hasObs = false;

        for (OssType type : types) {
            switch (type) {
                case LOCAL -> hasLocal = true;
                case ALIYUN -> hasAliyun = true;
                case QCLOUD -> hasQcloud = true;
                case QINIU -> hasQiniu = true;
                case MINIO -> hasMinio = true;
                case OBS -> hasObs = true;
            }
        }

        assertTrue(hasLocal, "应包含LOCAL");
        assertTrue(hasAliyun, "应包含ALIYUN");
        assertTrue(hasQcloud, "应包含QCLOUD");
        assertTrue(hasQiniu, "应包含QINIU");
        assertTrue(hasMinio, "应包含MINIO");
        assertTrue(hasObs, "应包含OBS");
    }

    // ==================== getByValue测试 ====================

    @Test
    @DisplayName("测试getByValue-local")
    public void testGetByValueLocal() {
        OssType type = OssType.getByValue("local");

        assertEquals(OssType.LOCAL, type, "local应返回LOCAL");
    }

    @Test
    @DisplayName("测试getByValue-aliyun")
    public void testGetByValueAliyun() {
        OssType type = OssType.getByValue("aliyun");

        assertEquals(OssType.ALIYUN, type, "aliyun应返回ALIYUN");
    }

    @Test
    @DisplayName("测试getByValue-qcloud")
    public void testGetByValueQcloud() {
        OssType type = OssType.getByValue("qcloud");

        assertEquals(OssType.QCLOUD, type, "qcloud应返回QCLOUD");
    }

    @Test
    @DisplayName("测试getByValue-qiniu")
    public void testGetByValueQiniu() {
        OssType type = OssType.getByValue("qiniu");

        assertEquals(OssType.QINIU, type, "qiniu应返回QINIU");
    }

    @Test
    @DisplayName("测试getByValue-minio")
    public void testGetByValueMinio() {
        OssType type = OssType.getByValue("minio");

        assertEquals(OssType.MINIO, type, "minio应返回MINIO");
    }

    @Test
    @DisplayName("测试getByValue-obs")
    public void testGetByValueObs() {
        OssType type = OssType.getByValue("obs");

        assertEquals(OssType.OBS, type, "obs应返回OBS");
    }

    @Test
    @DisplayName("测试getByValue-不存在的类型应抛出异常")
    public void testGetByValueNotExists() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> OssType.getByValue("unknown"),
            "不存在的类型应抛出IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("unknown"), "异常消息应包含未知类型值");
    }

    @Test
    @DisplayName("测试getByValue-null应抛出异常")
    public void testGetByValueNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.getByValue(null),
            "null值应抛出IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("测试getByValue-空字符串应抛出异常")
    public void testGetByValueEmpty() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.getByValue(""),
            "空字符串应抛出IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("测试getByValue-大小写敏感")
    public void testGetByValueCaseSensitive() {
        // 值是小写的，大写应找不到
        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.getByValue("LOCAL"),
            "应区分大小写"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.getByValue("Local"),
            "应区分大小写"
        );
    }

    // ==================== getValue测试 ====================

    @Test
    @DisplayName("测试getValue-所有枚举值")
    public void testGetValueAll() {
        assertEquals("local", OssType.LOCAL.getValue());
        assertEquals("aliyun", OssType.ALIYUN.getValue());
        assertEquals("qcloud", OssType.QCLOUD.getValue());
        assertEquals("qiniu", OssType.QINIU.getValue());
        assertEquals("minio", OssType.MINIO.getValue());
        assertEquals("obs", OssType.OBS.getValue());
    }

    @Test
    @DisplayName("测试getValue-值均为小写")
    public void testGetValueLowerCase() {
        for (OssType type : OssType.values()) {
            String value = type.getValue();
            assertEquals(value.toLowerCase(), value, type.name() + "的值应为小写");
        }
    }

    // ==================== valueOf测试 ====================

    @Test
    @DisplayName("测试valueOf-有效枚举名")
    public void testValueOfValid() {
        assertEquals(OssType.LOCAL, OssType.valueOf("LOCAL"));
        assertEquals(OssType.ALIYUN, OssType.valueOf("ALIYUN"));
        assertEquals(OssType.QCLOUD, OssType.valueOf("QCLOUD"));
        assertEquals(OssType.QINIU, OssType.valueOf("QINIU"));
        assertEquals(OssType.MINIO, OssType.valueOf("MINIO"));
        assertEquals(OssType.OBS, OssType.valueOf("OBS"));
    }

    @Test
    @DisplayName("测试valueOf-无效枚举名应抛出异常")
    public void testValueOfInvalid() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.valueOf("INVALID"),
            "无效枚举名应抛出异常"
        );
    }

    @Test
    @DisplayName("测试valueOf-小写枚举名应抛出异常")
    public void testValueOfLowerCase() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OssType.valueOf("local"),
            "小写枚举名应抛出异常"
        );
    }

    // ==================== ordinal测试 ====================

    @Test
    @DisplayName("测试ordinal-顺序")
    public void testOrdinal() {
        assertEquals(0, OssType.LOCAL.ordinal(), "LOCAL应为第一个");
        assertEquals(1, OssType.ALIYUN.ordinal(), "ALIYUN应为第二个");
        assertEquals(2, OssType.QCLOUD.ordinal(), "QCLOUD应为第三个");
        assertEquals(3, OssType.QINIU.ordinal(), "QINIU应为第四个");
        assertEquals(4, OssType.MINIO.ordinal(), "MINIO应为第五个");
        assertEquals(5, OssType.OBS.ordinal(), "OBS应为第六个");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-本地存储判断")
    public void testIsLocalStorage() {
        OssType type = OssType.LOCAL;

        boolean isLocal = "local".equals(type.getValue());

        assertTrue(isLocal, "应能判断是否为本地存储");
    }

    @Test
    @DisplayName("测试业务场景-云存储判断")
    public void testIsCloudStorage() {
        OssType[] cloudTypes = {OssType.ALIYUN, OssType.QCLOUD, OssType.QINIU, OssType.MINIO, OssType.OBS};

        for (OssType type : cloudTypes) {
            assertNotEquals("local", type.getValue(), type.name() + "应不是本地存储");
        }
    }

    @Test
    @DisplayName("测试业务场景-遍历所有类型")
    public void testIterateAllTypes() {
        StringBuilder sb = new StringBuilder();

        for (OssType type : OssType.values()) {
            sb.append(type.getValue()).append(",");
        }

        String result = sb.toString();
        assertTrue(result.contains("local,"), "应包含local");
        assertTrue(result.contains("aliyun,"), "应包含aliyun");
        assertTrue(result.contains("qcloud,"), "应包含qcloud");
        assertTrue(result.contains("qiniu,"), "应包含qiniu");
        assertTrue(result.contains("minio,"), "应包含minio");
        assertTrue(result.contains("obs,"), "应包含obs");
    }
}
