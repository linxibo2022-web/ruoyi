package plus.ruoyi.common.web.utils;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.RandomUtil;
import plus.ruoyi.common.core.utils.StringUtils;

import java.io.Serial;

/**
 * 数学运算验证码生成器
 * 生成加减乘运算表达式作为验证码
 *
 * @author Lion Li
 */
public class UnsignedMathGenerator implements CodeGenerator {

    @Serial
    private static final long serialVersionUID = -5514819971774091076L;

    /**
     * 支持的运算符：加、减、乘
     */
    private static final String OPERATORS = "+-*";

    /**
     * 参与计算的数字最大位数
     */
    private final int numberLength;

    /**
     * 默认构造函数，数字位数为2
     */
    public UnsignedMathGenerator() {
        this(2);
    }

    /**
     * 构造函数
     *
     * @param numberLength 参与计算的数字最大位数
     */
    public UnsignedMathGenerator(int numberLength) {
        this.numberLength = numberLength;
    }

    @Override
    public String generate() {
        final int limit = getLimit();
        int a = RandomUtil.randomInt(limit);
        int b = RandomUtil.randomInt(limit);

        // 确保大数在前，小数在后，避免负数结果
        String max = Integer.toString(Math.max(a, b));
        String min = Integer.toString(Math.min(a, b));

        // 右对齐填充空格，保持格式整齐
        max = StringUtils.rightPad(max, this.numberLength, CharUtil.SPACE);
        min = StringUtils.rightPad(min, this.numberLength, CharUtil.SPACE);

        // 生成表达式：大数 运算符 小数 =
        return max + RandomUtil.randomChar(OPERATORS) + min + '=';
    }

    @Override
    public boolean verify(String code, String userInputCode) {
        int result;
        try {
            // 解析用户输入的数字
            result = Integer.parseInt(userInputCode);
        } catch (NumberFormatException e) {
            // 用户输入非数字，验证失败
            return false;
        }

        // 计算表达式的正确结果
        final int calculateResult = (int) Calculator.conversion(code);
        return result == calculateResult;
    }

    /**
     * 获取验证码表达式的总长度
     *
     * @return 验证码长度
     */
    public int getLength() {
        // 长度 = 数字1长度 + 运算符长度 + 数字2长度 + 等号长度
        return this.numberLength * 2 + 2;
    }

    /**
     * 根据位数获取随机数的上限
     *
     * @return 数字上限值
     */
    private int getLimit() {
        // 例如：位数为2时，上限为100
        return Integer.parseInt("1" + StringUtils.repeat('0', this.numberLength));
    }
}
