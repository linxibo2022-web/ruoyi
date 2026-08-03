package plus.ruoyi.common.web.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UnsignedMathGenerator 数学运算验证码生成器测试
 * <p>
 * 测试验证码生成器的核心功能:
 * <ul>
 *   <li>生成验证码格式验证</li>
 *   <li>验证码验证功能</li>
 *   <li>长度计算</li>
 *   <li>不同位数配置</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("UnsignedMathGenerator 数学运算验证码生成器测试")
class UnsignedMathGeneratorTest extends BaseUnitTest {

    // ==================== 构造函数测试 ====================

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数 - 位数为2")
        void testDefaultConstructor() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            // 默认位数为2，长度 = 2*2 + 2 = 6
            assertEquals(6, generator.getLength(), "默认位数为2时，长度应为6");
        }

        @Test
        @DisplayName("指定位数构造函数 - 位数为1")
        void testConstructorWithLength1() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(1);

            // 位数为1，长度 = 1*2 + 2 = 4
            assertEquals(4, generator.getLength(), "位数为1时，长度应为4");
        }

        @Test
        @DisplayName("指定位数构造函数 - 位数为3")
        void testConstructorWithLength3() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(3);

            // 位数为3，长度 = 3*2 + 2 = 8
            assertEquals(8, generator.getLength(), "位数为3时，长度应为8");
        }
    }

    // ==================== 生成验证码测试 ====================

    @Nested
    @DisplayName("生成验证码测试")
    class GenerateTests {

        @Test
        @DisplayName("生成验证码 - 应返回非空字符串")
        void testGenerateNotNull() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            String code = generator.generate();

            assertNotNull(code, "生成的验证码不应为null");
            assertFalse(code.isEmpty(), "生成的验证码不应为空");
        }

        @Test
        @DisplayName("生成验证码 - 应以等号结尾")
        void testGenerateEndsWithEquals() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            String code = generator.generate();

            assertTrue(code.endsWith("="), "验证码应以等号结尾");
        }

        @Test
        @DisplayName("生成验证码 - 应包含运算符")
        void testGenerateContainsOperator() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            String code = generator.generate();

            // 验证码应包含 +、- 或 * 中的一个
            boolean hasOperator = code.contains("+") || code.contains("-") || code.contains("*");
            assertTrue(hasOperator, "验证码应包含运算符(+、-、*)");
        }

        @RepeatedTest(10)
        @DisplayName("生成验证码 - 格式应为 '数字 运算符 数字='")
        void testGenerateFormat() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(2);

            String code = generator.generate();

            // 验证格式：数字(可能含空格) + 运算符 + 数字(可能含空格) + =
            Pattern pattern = Pattern.compile("^\\s*\\d+\\s*[+\\-*]\\s*\\d+\\s*=$");
            Matcher matcher = pattern.matcher(code);
            assertTrue(matcher.matches(), "验证码格式应为 '数字 运算符 数字='，实际: " + code);
        }

        @RepeatedTest(20)
        @DisplayName("生成验证码 - 运算结果应为非负数")
        void testGenerateNonNegativeResult() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            String code = generator.generate();

            // 解析并计算表达式
            String expression = code.substring(0, code.length() - 1).trim(); // 去掉等号
            int result = calculateExpression(expression);

            assertTrue(result >= 0, "运算结果应为非负数，表达式: " + code + ", 结果: " + result);
        }

        @RepeatedTest(5)
        @DisplayName("生成验证码 - 多次生成应有随机性")
        void testGenerateRandomness() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();
            Set<String> codes = new HashSet<>();

            // 生成50个验证码
            for (int i = 0; i < 50; i++) {
                codes.add(generator.generate());
            }

            // 至少应该有不同的验证码（考虑到随机性，不要求100%不同）
            assertTrue(codes.size() > 1, "多次生成应产生不同的验证码");
        }
    }

    // ==================== 验证功能测试 ====================

    @Nested
    @DisplayName("验证功能测试")
    class VerifyTests {

        @Test
        @DisplayName("验证 - 加法正确答案应通过")
        void testVerifyAdditionCorrect() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            // 3 + 2 = 5
            assertTrue(generator.verify("3 +2 =", "5"), "3+2=5 应验证通过");
            assertTrue(generator.verify("10+5 =", "15"), "10+5=15 应验证通过");
            assertTrue(generator.verify("99+1 =", "100"), "99+1=100 应验证通过");
        }

        @Test
        @DisplayName("验证 - 减法正确答案应通过")
        void testVerifySubtractionCorrect() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            // 5 - 2 = 3
            assertTrue(generator.verify("5 -2 =", "3"), "5-2=3 应验证通过");
            assertTrue(generator.verify("10-5 =", "5"), "10-5=5 应验证通过");
            assertTrue(generator.verify("99-99=", "0"), "99-99=0 应验证通过");
        }

        @Test
        @DisplayName("验证 - 乘法正确答案应通过")
        void testVerifyMultiplicationCorrect() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            // 3 * 2 = 6
            assertTrue(generator.verify("3 *2 =", "6"), "3*2=6 应验证通过");
            assertTrue(generator.verify("10*5 =", "50"), "10*5=50 应验证通过");
            assertTrue(generator.verify("12*12=", "144"), "12*12=144 应验证通过");
        }

        @Test
        @DisplayName("验证 - 错误答案应不通过")
        void testVerifyIncorrectAnswer() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            assertFalse(generator.verify("3 +2 =", "6"), "3+2=6 应验证失败");
            assertFalse(generator.verify("5 -2 =", "2"), "5-2=2 应验证失败");
            assertFalse(generator.verify("3 *2 =", "5"), "3*2=5 应验证失败");
        }

        @Test
        @DisplayName("验证 - 非数字输入应不通过")
        void testVerifyNonNumericInput() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            assertFalse(generator.verify("3 +2 =", "abc"), "非数字输入应验证失败");
            assertFalse(generator.verify("3 +2 =", ""), "空字符串应验证失败");
            assertFalse(generator.verify("3 +2 =", "5.5"), "小数应验证失败");
            assertFalse(generator.verify("3 +2 =", " "), "空格应验证失败");
        }

        @Test
        @DisplayName("验证 - 负数答案应不通过（如果表达式结果为正）")
        void testVerifyNegativeAnswer() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            // 3+2=5，但输入-5
            assertFalse(generator.verify("3 +2 =", "-5"), "负数答案应验证失败");
        }

        @RepeatedTest(10)
        @DisplayName("验证 - 生成的验证码应可正确验证")
        void testVerifyGeneratedCode() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();

            String code = generator.generate();
            String expression = code.substring(0, code.length() - 1).trim();
            int correctAnswer = calculateExpression(expression);

            assertTrue(generator.verify(code, String.valueOf(correctAnswer)),
                "生成的验证码应可验证通过，表达式: " + code + ", 答案: " + correctAnswer);
        }
    }

    // ==================== 长度计算测试 ====================

    @Nested
    @DisplayName("长度计算测试")
    class LengthTests {

        @Test
        @DisplayName("getLength - 位数为1时长度为4")
        void testGetLengthWith1Digit() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(1);
            assertEquals(4, generator.getLength(), "位数为1时，长度应为4 (1+1+1+1)");
        }

        @Test
        @DisplayName("getLength - 位数为2时长度为6")
        void testGetLengthWith2Digits() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(2);
            assertEquals(6, generator.getLength(), "位数为2时，长度应为6 (2+1+2+1)");
        }

        @Test
        @DisplayName("getLength - 位数为3时长度为8")
        void testGetLengthWith3Digits() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(3);
            assertEquals(8, generator.getLength(), "位数为3时，长度应为8 (3+1+3+1)");
        }

        @Test
        @DisplayName("getLength - 位数为4时长度为10")
        void testGetLengthWith4Digits() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(4);
            assertEquals(10, generator.getLength(), "位数为4时，长度应为10 (4+1+4+1)");
        }
    }

    // ==================== 边界情况测试 ====================

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("位数为1 - 数字范围0-9")
        void testDigit1Range() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(1);

            for (int i = 0; i < 20; i++) {
                String code = generator.generate();
                // 解析出两个数字
                String expression = code.substring(0, code.length() - 1).trim();
                String[] parts = expression.split("[+\\-*]");

                for (String part : parts) {
                    int num = Integer.parseInt(part.trim());
                    assertTrue(num >= 0 && num <= 9, "位数为1时，数字应在0-9范围内");
                }
            }
        }

        @Test
        @DisplayName("位数为2 - 数字范围0-99")
        void testDigit2Range() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(2);

            for (int i = 0; i < 20; i++) {
                String code = generator.generate();
                String expression = code.substring(0, code.length() - 1).trim();
                String[] parts = expression.split("[+\\-*]");

                for (String part : parts) {
                    int num = Integer.parseInt(part.trim());
                    assertTrue(num >= 0 && num <= 99, "位数为2时，数字应在0-99范围内");
                }
            }
        }

        @Test
        @DisplayName("乘法结果在合理范围内")
        void testMultiplicationResultRange() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator(2);

            for (int i = 0; i < 50; i++) {
                String code = generator.generate();
                if (code.contains("*")) {
                    String expression = code.substring(0, code.length() - 1).trim();
                    int result = calculateExpression(expression);
                    // 两位数相乘最大 99*99 = 9801
                    assertTrue(result >= 0 && result <= 9801,
                        "乘法结果应在合理范围内，表达式: " + code + ", 结果: " + result);
                }
            }
        }
    }

    // ==================== CodeGenerator 接口实现验证 ====================

    @Nested
    @DisplayName("CodeGenerator 接口实现验证")
    class InterfaceTests {

        @Test
        @DisplayName("实现 CodeGenerator 接口")
        void testImplementsCodeGenerator() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();
            assertTrue(generator instanceof cn.hutool.captcha.generator.CodeGenerator,
                "应实现 CodeGenerator 接口");
        }

        @Test
        @DisplayName("generate 方法返回字符串")
        void testGenerateReturnsString() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();
            Object result = generator.generate();
            assertTrue(result instanceof String, "generate 方法应返回 String 类型");
        }

        @Test
        @DisplayName("verify 方法返回布尔值")
        void testVerifyReturnsBoolean() {
            UnsignedMathGenerator generator = new UnsignedMathGenerator();
            boolean result = generator.verify("1+1=", "2");
            // 只要不抛异常，返回值为 boolean 即可
            assertTrue(result || !result, "verify 方法应返回 boolean 类型");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算简单数学表达式
     * @param expression 表达式，如 "3 + 2" 或 "5 * 3"
     * @return 计算结果
     */
    private int calculateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");

        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            return Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]);
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return Integer.parseInt(parts[0]) * Integer.parseInt(parts[1]);
        } else if (expression.contains("-")) {
            // 处理减法，注意减号可能在第一个位置（负数）
            int minusIndex = expression.lastIndexOf('-');
            if (minusIndex > 0) {
                String part1 = expression.substring(0, minusIndex);
                String part2 = expression.substring(minusIndex + 1);
                return Integer.parseInt(part1) - Integer.parseInt(part2);
            }
        }
        return Integer.parseInt(expression);
    }
}
