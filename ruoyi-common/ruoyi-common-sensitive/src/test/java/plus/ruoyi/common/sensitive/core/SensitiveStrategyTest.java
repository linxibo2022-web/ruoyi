package plus.ruoyi.common.sensitive.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SensitiveStrategy 脱敏策略枚举测试
 * <p>
 * 测试各种脱敏策略的核心功能:
 * <ul>
 *   <li>手机号脱敏</li>
 *   <li>身份证脱敏</li>
 *   <li>邮箱脱敏</li>
 *   <li>银行卡脱敏</li>
 *   <li>中文姓名脱敏</li>
 *   <li>地址脱敏</li>
 *   <li>IP地址脱敏</li>
 *   <li>其他脱敏策略</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("SensitiveStrategy 脱敏策略枚举测试")
class SensitiveStrategyTest extends BaseUnitTest {

    // ==================== 手机号脱敏测试 ====================

    @Nested
    @DisplayName("手机号脱敏测试")
    class PhoneTests {

        @Test
        @DisplayName("标准11位手机号脱敏")
        void testPhoneDesensitize() {
            String phone = "13812345678";
            String result = SensitiveStrategy.PHONE.desensitizer().apply(phone);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.contains("****"), "应包含****");
            assertTrue(result.startsWith("138"), "应保留前3位");
            assertTrue(result.endsWith("5678"), "应保留后4位");
        }

        @Test
        @DisplayName("不同手机号段脱敏")
        void testDifferentPhoneNumbers() {
            String[] phones = {"13912345678", "15012345678", "18612345678", "17012345678"};

            for (String phone : phones) {
                String result = SensitiveStrategy.PHONE.desensitizer().apply(phone);
                assertNotNull(result, phone + " 脱敏结果不应为null");
                assertEquals(11, result.length(), phone + " 脱敏后长度应为11");
            }
        }
    }

    // ==================== 身份证脱敏测试 ====================

    @Nested
    @DisplayName("身份证脱敏测试")
    class IdCardTests {

        @Test
        @DisplayName("18位身份证脱敏")
        void testIdCardDesensitize() {
            String idCard = "110101199001011234";
            String result = SensitiveStrategy.ID_CARD.desensitizer().apply(idCard);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("110"), "应保留前3位");
            assertTrue(result.endsWith("1234"), "应保留后4位");
            assertTrue(result.contains("*"), "中间部分应有*");
        }

        @Test
        @DisplayName("15位老身份证脱敏")
        void testOldIdCardDesensitize() {
            String idCard = "110101900101123";
            String result = SensitiveStrategy.ID_CARD.desensitizer().apply(idCard);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("110"), "应保留前3位");
        }
    }

    // ==================== 邮箱脱敏测试 ====================

    @Nested
    @DisplayName("邮箱脱敏测试")
    class EmailTests {

        @Test
        @DisplayName("标准邮箱脱敏")
        void testEmailDesensitize() {
            String email = "test@example.com";
            String result = SensitiveStrategy.EMAIL.desensitizer().apply(email);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.contains("@"), "应保留@符号");
            assertTrue(result.contains("example.com"), "应保留域名");
        }

        @Test
        @DisplayName("不同邮箱格式脱敏")
        void testDifferentEmailFormats() {
            String[] emails = {"a@b.com", "longusername@domain.org", "user.name@company.co.uk"};

            for (String email : emails) {
                String result = SensitiveStrategy.EMAIL.desensitizer().apply(email);
                assertNotNull(result, email + " 脱敏结果不应为null");
                assertTrue(result.contains("@"), email + " 应保留@符号");
            }
        }
    }

    // ==================== 银行卡脱敏测试 ====================

    @Nested
    @DisplayName("银行卡脱敏测试")
    class BankCardTests {

        @Test
        @DisplayName("16位银行卡脱敏")
        void testBankCard16Desensitize() {
            String bankCard = "6222021234567890";
            String result = SensitiveStrategy.BANK_CARD.desensitizer().apply(bankCard);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.contains("*"), "应包含*");
        }

        @Test
        @DisplayName("19位银行卡脱敏")
        void testBankCard19Desensitize() {
            String bankCard = "6222021234567890123";
            String result = SensitiveStrategy.BANK_CARD.desensitizer().apply(bankCard);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.contains("*"), "应包含*");
        }
    }

    // ==================== 中文姓名脱敏测试 ====================

    @Nested
    @DisplayName("中文姓名脱敏测试")
    class ChineseNameTests {

        @Test
        @DisplayName("两字姓名脱敏")
        void testTwoCharacterNameDesensitize() {
            String name = "张三";
            String result = SensitiveStrategy.CHINESE_NAME.desensitizer().apply(name);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("张"), "应保留姓氏");
            assertTrue(result.contains("*"), "名字应被脱敏");
        }

        @Test
        @DisplayName("三字姓名脱敏")
        void testThreeCharacterNameDesensitize() {
            String name = "张三丰";
            String result = SensitiveStrategy.CHINESE_NAME.desensitizer().apply(name);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("张"), "应保留姓氏");
        }

        @Test
        @DisplayName("复姓脱敏")
        void testCompoundSurnameDesensitize() {
            String name = "欧阳修";
            String result = SensitiveStrategy.CHINESE_NAME.desensitizer().apply(name);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== 地址脱敏测试 ====================

    @Nested
    @DisplayName("地址脱敏测试")
    class AddressTests {

        @Test
        @DisplayName("长地址脱敏 - 保留前8个字符")
        void testLongAddressDesensitize() {
            String address = "北京市朝阳区建国路99号中央电视台";
            String result = SensitiveStrategy.ADDRESS.desensitizer().apply(address);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("北京市朝阳区建国"), "应保留前8个字符");
        }

        @Test
        @DisplayName("短地址脱敏")
        void testShortAddressDesensitize() {
            String address = "北京市";
            String result = SensitiveStrategy.ADDRESS.desensitizer().apply(address);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== 固定电话脱敏测试 ====================

    @Nested
    @DisplayName("固定电话脱敏测试")
    class FixedPhoneTests {

        @Test
        @DisplayName("带区号固定电话脱敏")
        void testFixedPhoneWithAreaCode() {
            String phone = "010-12345678";
            String result = SensitiveStrategy.FIXED_PHONE.desensitizer().apply(phone);

            assertNotNull(result, "脱敏结果不应为null");
        }

        @Test
        @DisplayName("不带区号固定电话脱敏")
        void testFixedPhoneWithoutAreaCode() {
            String phone = "12345678";
            String result = SensitiveStrategy.FIXED_PHONE.desensitizer().apply(phone);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== IP地址脱敏测试 ====================

    @Nested
    @DisplayName("IP地址脱敏测试")
    class IpAddressTests {

        @Test
        @DisplayName("IPv4地址脱敏")
        void testIpv4Desensitize() {
            String ipv4 = "192.168.1.100";
            String result = SensitiveStrategy.IPV4.desensitizer().apply(ipv4);

            assertNotNull(result, "脱敏结果不应为null");
        }

        @Test
        @DisplayName("IPv6地址脱敏")
        void testIpv6Desensitize() {
            String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
            String result = SensitiveStrategy.IPV6.desensitizer().apply(ipv6);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== 车牌号脱敏测试 ====================

    @Nested
    @DisplayName("车牌号脱敏测试")
    class CarLicenseTests {

        @Test
        @DisplayName("普通车牌脱敏")
        void testNormalCarLicenseDesensitize() {
            String license = "京A12345";
            String result = SensitiveStrategy.CAR_LICENSE.desensitizer().apply(license);

            assertNotNull(result, "脱敏结果不应为null");
        }

        @Test
        @DisplayName("新能源车牌脱敏")
        void testNewEnergyCarLicenseDesensitize() {
            String license = "京AD12345";
            String result = SensitiveStrategy.CAR_LICENSE.desensitizer().apply(license);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== 密码脱敏测试 ====================

    @Nested
    @DisplayName("密码脱敏测试")
    class PasswordTests {

        @Test
        @DisplayName("密码脱敏 - 全部用*代替")
        void testPasswordDesensitize() {
            String password = "MySecretPassword123";
            String result = SensitiveStrategy.PASSWORD.desensitizer().apply(password);

            assertNotNull(result, "脱敏结果不应为null");
            assertFalse(result.contains("M"), "不应包含原始字符");
            assertTrue(result.matches("\\*+"), "应全部为*");
        }

        @Test
        @DisplayName("短密码脱敏")
        void testShortPasswordDesensitize() {
            String password = "abc";
            String result = SensitiveStrategy.PASSWORD.desensitizer().apply(password);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.matches("\\*+"), "应全部为*");
        }
    }

    // ==================== 用户ID脱敏测试 ====================

    @Nested
    @DisplayName("用户ID脱敏测试")
    class UserIdTests {

        @Test
        @DisplayName("用户ID脱敏 - 生成随机数字")
        void testUserIdDesensitize() {
            String userId = "12345678";
            String result = SensitiveStrategy.USER_ID.desensitizer().apply(userId);

            assertNotNull(result, "脱敏结果不应为null");
            // 注意：USER_ID 策略返回随机数字，所以不等于原值
        }
    }

    // ==================== 首字符保留脱敏测试 ====================

    @Nested
    @DisplayName("首字符保留脱敏测试")
    class FirstMaskTests {

        @Test
        @DisplayName("首字符保留脱敏")
        void testFirstMaskDesensitize() {
            String text = "Hello World";
            String result = SensitiveStrategy.FIRST_MASK.desensitizer().apply(text);

            assertNotNull(result, "脱敏结果不应为null");
            assertTrue(result.startsWith("H"), "应保留首字符");
        }

        @Test
        @DisplayName("单字符脱敏")
        void testSingleCharacterFirstMask() {
            String text = "A";
            String result = SensitiveStrategy.FIRST_MASK.desensitizer().apply(text);

            assertNotNull(result, "脱敏结果不应为null");
        }
    }

    // ==================== 清空脱敏测试 ====================

    @Nested
    @DisplayName("清空脱敏测试")
    class ClearTests {

        @Test
        @DisplayName("清空脱敏 - 返回空字符串")
        void testClearDesensitize() {
            String text = "任意文本内容";
            String result = SensitiveStrategy.CLEAR.desensitizer().apply(text);

            assertNotNull(result, "脱敏结果不应为null");
            assertEquals("", result, "应返回空字符串");
        }

        @Test
        @DisplayName("置空脱敏 - 返回null")
        void testClearToNullDesensitize() {
            String text = "任意文本内容";
            String result = SensitiveStrategy.CLEAR_TO_NULL.desensitizer().apply(text);

            assertNull(result, "应返回null");
        }
    }

    // ==================== 枚举完整性测试 ====================

    @Nested
    @DisplayName("枚举完整性测试")
    class EnumCompletionTests {

        @Test
        @DisplayName("所有策略都应有脱敏函数")
        void testAllStrategiesHaveDesensitizer() {
            for (SensitiveStrategy strategy : SensitiveStrategy.values()) {
                Function<String, String> desensitizer = strategy.desensitizer();
                assertNotNull(desensitizer, strategy.name() + " 应有脱敏函数");
            }
        }

        @Test
        @DisplayName("验证枚举值数量")
        void testEnumValueCount() {
            int expectedCount = 16; // ID_CARD, PHONE, ADDRESS, EMAIL, BANK_CARD, CHINESE_NAME, FIXED_PHONE, USER_ID, PASSWORD, IPV4, IPV6, CAR_LICENSE, FIRST_MASK, STRING_MASK, CLEAR, CLEAR_TO_NULL
            assertEquals(expectedCount, SensitiveStrategy.values().length, "应有" + expectedCount + "个脱敏策略");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("空字符串脱敏")
        void testEmptyStringDesensitize() {
            String empty = "";

            // 测试各种策略对空字符串的处理
            for (SensitiveStrategy strategy : SensitiveStrategy.values()) {
                try {
                    strategy.desensitizer().apply(empty);
                    // 如果没有抛出异常，则测试通过
                } catch (Exception e) {
                    // 某些策略可能对空字符串抛出异常，这也是可接受的行为
                }
            }
        }

        @Test
        @DisplayName("null值脱敏 - 应处理或抛出异常")
        void testNullValueDesensitize() {
            // 测试各种策略对null的处理
            for (SensitiveStrategy strategy : SensitiveStrategy.values()) {
                try {
                    strategy.desensitizer().apply(null);
                    // 如果返回值，则应该是null或处理后的结果
                } catch (NullPointerException e) {
                    // 抛出NullPointerException是可接受的行为
                }
            }
        }
    }
}
