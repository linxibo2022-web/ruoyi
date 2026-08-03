package plus.ruoyi.common.web.enums;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.base.BaseUnitTest;
import plus.ruoyi.common.web.utils.UnsignedMathGenerator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证码枚举测试
 * <p>
 * 测试验证码类型和类别枚举的核心功能:
 * <ul>
 *   <li>CaptchaType - 验证码生成类型</li>
 *   <li>CaptchaCategory - 验证码干扰类别</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("验证码枚举测试")
class CaptchaEnumsTest extends BaseUnitTest {

    // ==================== CaptchaType 测试 ====================

    @Nested
    @DisplayName("CaptchaType 验证码类型测试")
    class CaptchaTypeTests {

        @Test
        @DisplayName("MATH类型 - 应返回UnsignedMathGenerator类")
        void testMathType() {
            CaptchaType type = CaptchaType.MATH;

            assertEquals(UnsignedMathGenerator.class, type.getClazz(),
                "MATH类型应对应UnsignedMathGenerator");
        }

        @Test
        @DisplayName("CHAR类型 - 应返回RandomGenerator类")
        void testCharType() {
            CaptchaType type = CaptchaType.CHAR;

            assertEquals(RandomGenerator.class, type.getClazz(),
                "CHAR类型应对应RandomGenerator");
        }

        @Test
        @DisplayName("所有类型的clazz应实现CodeGenerator接口")
        void testAllTypesImplementCodeGenerator() {
            for (CaptchaType type : CaptchaType.values()) {
                assertTrue(CodeGenerator.class.isAssignableFrom(type.getClazz()),
                    type.name() + " 的生成器类应实现CodeGenerator接口");
            }
        }

        @Test
        @DisplayName("枚举值数量应为2")
        void testEnumCount() {
            assertEquals(2, CaptchaType.values().length, "CaptchaType应有2个枚举值");
        }

        @Test
        @DisplayName("枚举name应正确")
        void testEnumNames() {
            assertEquals("MATH", CaptchaType.MATH.name());
            assertEquals("CHAR", CaptchaType.CHAR.name());
        }

        @Test
        @DisplayName("valueOf应正确解析枚举")
        void testValueOf() {
            assertEquals(CaptchaType.MATH, CaptchaType.valueOf("MATH"));
            assertEquals(CaptchaType.CHAR, CaptchaType.valueOf("CHAR"));
        }

        @Test
        @DisplayName("valueOf无效值应抛出异常")
        void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> CaptchaType.valueOf("INVALID"),
                "无效的枚举值应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("MATH生成器应可实例化")
        void testMathGeneratorInstantiation() throws Exception {
            Class<? extends CodeGenerator> clazz = CaptchaType.MATH.getClazz();
            CodeGenerator generator = clazz.getDeclaredConstructor().newInstance();

            assertNotNull(generator, "MATH生成器应可实例化");
            assertTrue(generator instanceof UnsignedMathGenerator);
        }

        @Test
        @DisplayName("CHAR生成器应可实例化")
        void testCharGeneratorInstantiation() throws Exception {
            Class<? extends CodeGenerator> clazz = CaptchaType.CHAR.getClazz();
            // RandomGenerator 需要长度参数
            CodeGenerator generator = clazz.getDeclaredConstructor(int.class).newInstance(4);

            assertNotNull(generator, "CHAR生成器应可实例化");
            assertTrue(generator instanceof RandomGenerator);
        }
    }

    // ==================== CaptchaCategory 测试 ====================

    @Nested
    @DisplayName("CaptchaCategory 验证码类别测试")
    class CaptchaCategoryTests {

        @Test
        @DisplayName("LINE类别 - 应返回LineCaptcha类")
        void testLineCategory() {
            CaptchaCategory category = CaptchaCategory.LINE;

            assertEquals(LineCaptcha.class, category.getClazz(),
                "LINE类别应对应LineCaptcha");
        }

        @Test
        @DisplayName("CIRCLE类别 - 应返回CircleCaptcha类")
        void testCircleCategory() {
            CaptchaCategory category = CaptchaCategory.CIRCLE;

            assertEquals(CircleCaptcha.class, category.getClazz(),
                "CIRCLE类别应对应CircleCaptcha");
        }

        @Test
        @DisplayName("SHEAR类别 - 应返回ShearCaptcha类")
        void testShearCategory() {
            CaptchaCategory category = CaptchaCategory.SHEAR;

            assertEquals(ShearCaptcha.class, category.getClazz(),
                "SHEAR类别应对应ShearCaptcha");
        }

        @Test
        @DisplayName("所有类别的clazz应继承AbstractCaptcha")
        void testAllCategoriesExtendAbstractCaptcha() {
            for (CaptchaCategory category : CaptchaCategory.values()) {
                assertTrue(AbstractCaptcha.class.isAssignableFrom(category.getClazz()),
                    category.name() + " 的验证码类应继承AbstractCaptcha");
            }
        }

        @Test
        @DisplayName("枚举值数量应为3")
        void testEnumCount() {
            assertEquals(3, CaptchaCategory.values().length, "CaptchaCategory应有3个枚举值");
        }

        @Test
        @DisplayName("枚举name应正确")
        void testEnumNames() {
            assertEquals("LINE", CaptchaCategory.LINE.name());
            assertEquals("CIRCLE", CaptchaCategory.CIRCLE.name());
            assertEquals("SHEAR", CaptchaCategory.SHEAR.name());
        }

        @Test
        @DisplayName("valueOf应正确解析枚举")
        void testValueOf() {
            assertEquals(CaptchaCategory.LINE, CaptchaCategory.valueOf("LINE"));
            assertEquals(CaptchaCategory.CIRCLE, CaptchaCategory.valueOf("CIRCLE"));
            assertEquals(CaptchaCategory.SHEAR, CaptchaCategory.valueOf("SHEAR"));
        }

        @Test
        @DisplayName("valueOf无效值应抛出异常")
        void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> CaptchaCategory.valueOf("INVALID"),
                "无效的枚举值应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("ordinal顺序应正确")
        void testOrdinal() {
            assertEquals(0, CaptchaCategory.LINE.ordinal());
            assertEquals(1, CaptchaCategory.CIRCLE.ordinal());
            assertEquals(2, CaptchaCategory.SHEAR.ordinal());
        }

        @Test
        @DisplayName("LINE验证码应可实例化")
        void testLineCaptchaInstantiation() throws Exception {
            Class<? extends AbstractCaptcha> clazz = CaptchaCategory.LINE.getClazz();
            // LineCaptcha 需要 width, height 参数
            AbstractCaptcha captcha = clazz.getDeclaredConstructor(int.class, int.class)
                .newInstance(200, 100);

            assertNotNull(captcha, "LINE验证码应可实例化");
            assertTrue(captcha instanceof LineCaptcha);
        }

        @Test
        @DisplayName("CIRCLE验证码应可实例化")
        void testCircleCaptchaInstantiation() throws Exception {
            Class<? extends AbstractCaptcha> clazz = CaptchaCategory.CIRCLE.getClazz();
            AbstractCaptcha captcha = clazz.getDeclaredConstructor(int.class, int.class)
                .newInstance(200, 100);

            assertNotNull(captcha, "CIRCLE验证码应可实例化");
            assertTrue(captcha instanceof CircleCaptcha);
        }

        @Test
        @DisplayName("SHEAR验证码应可实例化")
        void testShearCaptchaInstantiation() throws Exception {
            Class<? extends AbstractCaptcha> clazz = CaptchaCategory.SHEAR.getClazz();
            AbstractCaptcha captcha = clazz.getDeclaredConstructor(int.class, int.class)
                .newInstance(200, 100);

            assertNotNull(captcha, "SHEAR验证码应可实例化");
            assertTrue(captcha instanceof ShearCaptcha);
        }
    }

    // ==================== 综合测试 ====================

    @Nested
    @DisplayName("综合功能测试")
    class IntegrationTests {

        @Test
        @DisplayName("CaptchaType和CaptchaCategory应可组合使用")
        void testTypeAndCategoryComposition() throws Exception {
            // 验证任意类型和类别的组合都能正常工作
            for (CaptchaType type : CaptchaType.values()) {
                for (CaptchaCategory category : CaptchaCategory.values()) {
                    // 类型和类别都有有效的Class
                    assertNotNull(type.getClazz(), type.name() + " 应有有效的生成器类");
                    assertNotNull(category.getClazz(), category.name() + " 应有有效的验证码类");

                    // 生成器类实现CodeGenerator
                    assertTrue(CodeGenerator.class.isAssignableFrom(type.getClazz()));
                    // 验证码类继承AbstractCaptcha
                    assertTrue(AbstractCaptcha.class.isAssignableFrom(category.getClazz()));
                }
            }
        }

        @Test
        @DisplayName("枚举应支持switch表达式")
        void testSwitchExpression() {
            CaptchaType type = CaptchaType.MATH;
            String result = switch (type) {
                case MATH -> "数学运算";
                case CHAR -> "随机字符";
            };
            assertEquals("数学运算", result);

            CaptchaCategory category = CaptchaCategory.LINE;
            String categoryResult = switch (category) {
                case LINE -> "线段干扰";
                case CIRCLE -> "圆圈干扰";
                case SHEAR -> "扭曲干扰";
            };
            assertEquals("线段干扰", categoryResult);
        }
    }
}
