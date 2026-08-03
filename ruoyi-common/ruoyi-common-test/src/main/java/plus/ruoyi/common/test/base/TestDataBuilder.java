package plus.ruoyi.common.test.base;

import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 测试数据构造器
 * <p>
 * 提供快速生成测试数据的工具方法
 *
 * <p>使用示例:
 * <pre>
 * // 生成随机用户名
 * String userName = TestDataBuilder.randomUserName();
 *
 * // 生成随机手机号
 * String phone = TestDataBuilder.randomPhone();
 *
 * // 生成随机日期
 * LocalDateTime date = TestDataBuilder.randomDateTime();
 *
 * // 生成随机列表
 * List&lt;String&gt; list = TestDataBuilder.randomList(5, TestDataBuilder::randomUserName);
 * </pre>
 *
 * @author 抓蛙师
 */
public class TestDataBuilder {

    private static final Faker FAKER = new Faker(Locale.CHINA);
    private static final Random RANDOM = new Random();

    /**
     * 生成随机用户名
     */
    public static String randomUserName() {
        return FAKER.name().username();
    }

    /**
     * 生成随机中文姓名
     */
    public static String randomChineseName() {
        return FAKER.name().lastName() + FAKER.name().firstName();
    }

    /**
     * 生成随机手机号(11位)
     * <p>格式: 1[3-9]xxxxxxxxx
     */
    public static String randomPhone() {
        return "1" + (3 + RANDOM.nextInt(7)) + String.format("%09d", RANDOM.nextInt(1_000_000_000));
    }

    /**
     * 生成随机邮箱
     */
    public static String randomEmail() {
        return FAKER.internet().emailAddress();
    }

    /**
     * 生成随机密码
     */
    public static String randomPassword() {
        return FAKER.internet().password(8, 16, true, true);
    }

    /**
     * 生成随机地址
     */
    public static String randomAddress() {
        return FAKER.address().fullAddress();
    }

    /**
     * 生成随机公司名
     */
    public static String randomCompany() {
        return FAKER.company().name();
    }

    /**
     * 生成随机URL
     */
    public static String randomUrl() {
        return FAKER.internet().url();
    }

    /**
     * 生成随机IP
     */
    public static String randomIp() {
        return FAKER.internet().ipV4Address();
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     */
    public static String randomString(int length) {
        return FAKER.lorem().characters(length);
    }

    /**
     * 生成随机整数
     *
     * @param min 最小值
     * @param max 最大值
     */
    public static int randomInt(int min, int max) {
        return FAKER.number().numberBetween(min, max);
    }

    /**
     * 生成随机Long
     *
     * @param min 最小值
     * @param max 最大值
     */
    public static long randomLong(long min, long max) {
        return FAKER.number().numberBetween(min, max);
    }

    /**
     * 生成随机布尔值
     */
    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    /**
     * 生成随机日期时间
     */
    public static LocalDateTime randomDateTime() {
        Date date = FAKER.date().past(365, TimeUnit.DAYS);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 生成随机日期时间(指定范围)
     *
     * @param days 过去多少天内
     */
    public static LocalDateTime randomDateTime(int days) {
        Date date = FAKER.date().past(days, TimeUnit.DAYS);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 生成随机未来日期
     *
     * @param days 未来多少天内
     */
    public static LocalDateTime randomFutureDateTime(int days) {
        Date date = FAKER.date().future(days, TimeUnit.DAYS);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 从数组中随机选择一个元素
     */
    @SafeVarargs
    public static <T> T randomChoice(T... items) {
        if (items == null || items.length == 0) {
            return null;
        }
        return items[RANDOM.nextInt(items.length)];
    }

    /**
     * 从列表中随机选择一个元素
     */
    public static <T> T randomChoice(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * 生成随机列表
     *
     * @param size      列表大小
     * @param generator 元素生成器
     */
    public static <T> List<T> randomList(int size, java.util.function.Supplier<T> generator) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(generator.get());
        }
        return list;
    }

    /**
     * 生成随机Set
     *
     * @param size      集合大小
     * @param generator 元素生成器
     */
    public static <T> Set<T> randomSet(int size, java.util.function.Supplier<T> generator) {
        Set<T> set = new HashSet<>(size);
        while (set.size() < size) {
            set.add(generator.get());
        }
        return set;
    }

    /**
     * 生成随机ID
     */
    public static Long randomId() {
        return randomLong(1L, 1000000L);
    }

    /**
     * 生成随机状态(0或1)
     */
    public static String randomStatus() {
        return randomChoice("0", "1");
    }

    /**
     * 生成随机删除标志(0-正常,1-删除)
     */
    public static String randomDelFlag() {
        return randomChoice("0", "1");
    }
}
