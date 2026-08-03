package plus.ruoyi.common.oss.enums;

import plus.ruoyi.common.oss.exception.OssException;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessPolicyType 访问策略类型枚举测试
 *
 * @author 抓蛙师
 */
@DisplayName("AccessPolicyType访问策略类型枚举测试")
public class AccessPolicyTypeTest extends BaseUnitTest {

    // ==================== 枚举值测试 ====================

    @Test
    @DisplayName("测试枚举值-PRIVATE私有")
    public void testPrivateType() {
        AccessPolicyType type = AccessPolicyType.PRIVATE;

        assertEquals("0", type.getType(), "PRIVATE类型值应为0");
        assertEquals(BucketCannedACL.PRIVATE, type.getBucketCannedACL(), "桶ACL应为PRIVATE");
        assertEquals(ObjectCannedACL.PRIVATE, type.getObjectCannedACL(), "对象ACL应为PRIVATE");
    }

    @Test
    @DisplayName("测试枚举值-PUBLIC公共读写")
    public void testPublicType() {
        AccessPolicyType type = AccessPolicyType.PUBLIC;

        assertEquals("1", type.getType(), "PUBLIC类型值应为1");
        assertEquals(BucketCannedACL.PUBLIC_READ_WRITE, type.getBucketCannedACL(), "桶ACL应为PUBLIC_READ_WRITE");
        assertEquals(ObjectCannedACL.PUBLIC_READ_WRITE, type.getObjectCannedACL(), "对象ACL应为PUBLIC_READ_WRITE");
    }

    @Test
    @DisplayName("测试枚举值-CUSTOM公共读")
    public void testCustomType() {
        AccessPolicyType type = AccessPolicyType.CUSTOM;

        assertEquals("2", type.getType(), "CUSTOM类型值应为2");
        assertEquals(BucketCannedACL.PUBLIC_READ, type.getBucketCannedACL(), "桶ACL应为PUBLIC_READ");
        assertEquals(ObjectCannedACL.PUBLIC_READ, type.getObjectCannedACL(), "对象ACL应为PUBLIC_READ");
    }

    // ==================== values()测试 ====================

    @Test
    @DisplayName("测试values-枚举数量")
    public void testValuesCount() {
        AccessPolicyType[] types = AccessPolicyType.values();

        assertEquals(3, types.length, "应有3种访问策略类型");
    }

    @Test
    @DisplayName("测试values-包含所有类型")
    public void testValuesContainsAll() {
        AccessPolicyType[] types = AccessPolicyType.values();

        boolean hasPrivate = false;
        boolean hasPublic = false;
        boolean hasCustom = false;

        for (AccessPolicyType type : types) {
            switch (type) {
                case PRIVATE -> hasPrivate = true;
                case PUBLIC -> hasPublic = true;
                case CUSTOM -> hasCustom = true;
            }
        }

        assertTrue(hasPrivate, "应包含PRIVATE");
        assertTrue(hasPublic, "应包含PUBLIC");
        assertTrue(hasCustom, "应包含CUSTOM");
    }

    // ==================== getByType测试 ====================

    @Test
    @DisplayName("测试getByType-0私有")
    public void testGetByType0() {
        AccessPolicyType type = AccessPolicyType.getByType("0");

        assertEquals(AccessPolicyType.PRIVATE, type, "0应返回PRIVATE");
    }

    @Test
    @DisplayName("测试getByType-1公共读写")
    public void testGetByType1() {
        AccessPolicyType type = AccessPolicyType.getByType("1");

        assertEquals(AccessPolicyType.PUBLIC, type, "1应返回PUBLIC");
    }

    @Test
    @DisplayName("测试getByType-2公共读")
    public void testGetByType2() {
        AccessPolicyType type = AccessPolicyType.getByType("2");

        assertEquals(AccessPolicyType.CUSTOM, type, "2应返回CUSTOM");
    }

    @Test
    @DisplayName("测试getByType-不存在的类型应抛出OssException")
    public void testGetByTypeNotExists() {
        OssException exception = assertThrows(
            OssException.class,
            () -> AccessPolicyType.getByType("3"),
            "不存在的类型应抛出OssException"
        );

        assertTrue(exception.getMessage().contains("3"), "异常消息应包含未知类型值");
    }

    @Test
    @DisplayName("测试getByType-null应抛出OssException")
    public void testGetByTypeNull() {
        assertThrows(
            OssException.class,
            () -> AccessPolicyType.getByType(null),
            "null值应抛出OssException"
        );
    }

    @Test
    @DisplayName("测试getByType-空字符串应抛出OssException")
    public void testGetByTypeEmpty() {
        assertThrows(
            OssException.class,
            () -> AccessPolicyType.getByType(""),
            "空字符串应抛出OssException"
        );
    }

    @Test
    @DisplayName("测试getByType-负数应抛出OssException")
    public void testGetByTypeNegative() {
        assertThrows(
            OssException.class,
            () -> AccessPolicyType.getByType("-1"),
            "负数应抛出OssException"
        );
    }

    @Test
    @DisplayName("测试getByType-非数字应抛出OssException")
    public void testGetByTypeNonNumeric() {
        assertThrows(
            OssException.class,
            () -> AccessPolicyType.getByType("abc"),
            "非数字应抛出OssException"
        );
    }

    // ==================== getType测试 ====================

    @Test
    @DisplayName("测试getType-所有枚举值")
    public void testGetTypeAll() {
        assertEquals("0", AccessPolicyType.PRIVATE.getType());
        assertEquals("1", AccessPolicyType.PUBLIC.getType());
        assertEquals("2", AccessPolicyType.CUSTOM.getType());
    }

    // ==================== getBucketCannedACL测试 ====================

    @Test
    @DisplayName("测试getBucketCannedACL-所有枚举值")
    public void testGetBucketCannedACLAll() {
        assertEquals(BucketCannedACL.PRIVATE, AccessPolicyType.PRIVATE.getBucketCannedACL());
        assertEquals(BucketCannedACL.PUBLIC_READ_WRITE, AccessPolicyType.PUBLIC.getBucketCannedACL());
        assertEquals(BucketCannedACL.PUBLIC_READ, AccessPolicyType.CUSTOM.getBucketCannedACL());
    }

    @Test
    @DisplayName("测试getBucketCannedACL-不为null")
    public void testGetBucketCannedACLNotNull() {
        for (AccessPolicyType type : AccessPolicyType.values()) {
            assertNotNull(type.getBucketCannedACL(), type.name() + "的BucketCannedACL不应为null");
        }
    }

    // ==================== getObjectCannedACL测试 ====================

    @Test
    @DisplayName("测试getObjectCannedACL-所有枚举值")
    public void testGetObjectCannedACLAll() {
        assertEquals(ObjectCannedACL.PRIVATE, AccessPolicyType.PRIVATE.getObjectCannedACL());
        assertEquals(ObjectCannedACL.PUBLIC_READ_WRITE, AccessPolicyType.PUBLIC.getObjectCannedACL());
        assertEquals(ObjectCannedACL.PUBLIC_READ, AccessPolicyType.CUSTOM.getObjectCannedACL());
    }

    @Test
    @DisplayName("测试getObjectCannedACL-不为null")
    public void testGetObjectCannedACLNotNull() {
        for (AccessPolicyType type : AccessPolicyType.values()) {
            assertNotNull(type.getObjectCannedACL(), type.name() + "的ObjectCannedACL不应为null");
        }
    }

    // ==================== valueOf测试 ====================

    @Test
    @DisplayName("测试valueOf-有效枚举名")
    public void testValueOfValid() {
        assertEquals(AccessPolicyType.PRIVATE, AccessPolicyType.valueOf("PRIVATE"));
        assertEquals(AccessPolicyType.PUBLIC, AccessPolicyType.valueOf("PUBLIC"));
        assertEquals(AccessPolicyType.CUSTOM, AccessPolicyType.valueOf("CUSTOM"));
    }

    @Test
    @DisplayName("测试valueOf-无效枚举名应抛出异常")
    public void testValueOfInvalid() {
        assertThrows(
            IllegalArgumentException.class,
            () -> AccessPolicyType.valueOf("INVALID"),
            "无效枚举名应抛出异常"
        );
    }

    @Test
    @DisplayName("测试valueOf-小写枚举名应抛出异常")
    public void testValueOfLowerCase() {
        assertThrows(
            IllegalArgumentException.class,
            () -> AccessPolicyType.valueOf("private"),
            "小写枚举名应抛出异常"
        );
    }

    // ==================== ordinal测试 ====================

    @Test
    @DisplayName("测试ordinal-顺序")
    public void testOrdinal() {
        assertEquals(0, AccessPolicyType.PRIVATE.ordinal(), "PRIVATE应为第一个");
        assertEquals(1, AccessPolicyType.PUBLIC.ordinal(), "PUBLIC应为第二个");
        assertEquals(2, AccessPolicyType.CUSTOM.ordinal(), "CUSTOM应为第三个");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-从数据库值获取枚举")
    public void testGetFromDatabaseValue() {
        // 模拟从数据库读取的值
        String dbValue = "0";
        AccessPolicyType type = AccessPolicyType.getByType(dbValue);

        assertEquals(AccessPolicyType.PRIVATE, type);
        assertEquals(BucketCannedACL.PRIVATE, type.getBucketCannedACL());
    }

    @Test
    @DisplayName("测试业务场景-私有桶配置")
    public void testPrivateBucketConfig() {
        AccessPolicyType type = AccessPolicyType.PRIVATE;

        // 私有桶的ACL应该都是PRIVATE
        assertEquals(BucketCannedACL.PRIVATE, type.getBucketCannedACL(), "私有桶ACL应为PRIVATE");
        assertEquals(ObjectCannedACL.PRIVATE, type.getObjectCannedACL(), "私有对象ACL应为PRIVATE");
    }

    @Test
    @DisplayName("测试业务场景-公开桶配置")
    public void testPublicBucketConfig() {
        AccessPolicyType type = AccessPolicyType.PUBLIC;

        // 公开桶支持读写
        assertEquals(BucketCannedACL.PUBLIC_READ_WRITE, type.getBucketCannedACL());
        assertEquals(ObjectCannedACL.PUBLIC_READ_WRITE, type.getObjectCannedACL());
    }

    @Test
    @DisplayName("测试业务场景-自定义桶配置(公共读)")
    public void testCustomBucketConfig() {
        AccessPolicyType type = AccessPolicyType.CUSTOM;

        // 自定义通常用于公共读私有写
        assertEquals(BucketCannedACL.PUBLIC_READ, type.getBucketCannedACL());
        assertEquals(ObjectCannedACL.PUBLIC_READ, type.getObjectCannedACL());
    }

    @Test
    @DisplayName("测试业务场景-遍历所有策略类型")
    public void testIterateAllTypes() {
        StringBuilder sb = new StringBuilder();

        for (AccessPolicyType type : AccessPolicyType.values()) {
            sb.append(type.getType()).append(":");
            sb.append(type.name()).append(",");
        }

        String result = sb.toString();
        assertTrue(result.contains("0:PRIVATE"), "应包含PRIVATE");
        assertTrue(result.contains("1:PUBLIC"), "应包含PUBLIC");
        assertTrue(result.contains("2:CUSTOM"), "应包含CUSTOM");
    }

    @Test
    @DisplayName("测试业务场景-类型值转ACL字符串")
    public void testTypeToAclString() {
        for (AccessPolicyType type : AccessPolicyType.values()) {
            String bucketAcl = type.getBucketCannedACL().toString();
            String objectAcl = type.getObjectCannedACL().toString();

            assertNotNull(bucketAcl, type.name() + "桶ACL字符串不应为null");
            assertNotNull(objectAcl, type.name() + "对象ACL字符串不应为null");
        }
    }

    // ==================== ACL一致性测试 ====================

    @Test
    @DisplayName("测试ACL一致性-PRIVATE桶和对象ACL应匹配")
    public void testPrivateAclConsistency() {
        AccessPolicyType type = AccessPolicyType.PRIVATE;

        // PRIVATE的桶ACL和对象ACL应该都是PRIVATE级别
        String bucketAclName = type.getBucketCannedACL().toString();
        String objectAclName = type.getObjectCannedACL().toString();

        assertTrue(bucketAclName.toLowerCase().contains("private"), "桶ACL应包含private");
        assertTrue(objectAclName.toLowerCase().contains("private"), "对象ACL应包含private");
    }

    @Test
    @DisplayName("测试ACL一致性-PUBLIC桶和对象ACL应匹配")
    public void testPublicAclConsistency() {
        AccessPolicyType type = AccessPolicyType.PUBLIC;

        String bucketAclName = type.getBucketCannedACL().toString();
        String objectAclName = type.getObjectCannedACL().toString();

        assertTrue(bucketAclName.toLowerCase().contains("public"), "桶ACL应包含public");
        assertTrue(objectAclName.toLowerCase().contains("public"), "对象ACL应包含public");
    }

    @Test
    @DisplayName("测试ACL一致性-CUSTOM桶和对象ACL应匹配")
    public void testCustomAclConsistency() {
        AccessPolicyType type = AccessPolicyType.CUSTOM;

        String bucketAclName = type.getBucketCannedACL().toString();
        String objectAclName = type.getObjectCannedACL().toString();

        // CUSTOM是公共读
        assertTrue(bucketAclName.toLowerCase().contains("public"), "桶ACL应包含public");
        assertTrue(objectAclName.toLowerCase().contains("public"), "对象ACL应包含public");
    }
}
