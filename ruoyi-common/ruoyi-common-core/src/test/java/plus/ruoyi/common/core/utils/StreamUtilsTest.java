package plus.ruoyi.common.core.utils;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StreamUtils 流工具类测试
 *
 * @author 抓蛙师
 */
@Slf4j
@DisplayName("流工具类测试")
public class StreamUtilsTest extends BaseUnitTest {

    @Test
    @DisplayName("测试filter-过滤集合")
    public void testFilter() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = StreamUtils.filter(list, num -> num > 2);

        assertEquals(3, result.size());
        assertTrue(result.contains(3));
        assertTrue(result.contains(4));
        assertTrue(result.contains(5));
    }

    @Test
    @DisplayName("测试filter-空集合")
    public void testFilterEmptyCollection() {
        List<Integer> result = StreamUtils.filter(null, num -> num > 2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试findFirst-查找第一个元素")
    public void testFindFirst() {
        List<String> list = Arrays.asList("apple", "banana", "apricot");
        Optional<String> result = StreamUtils.findFirst(list, s -> s.startsWith("a"));

        assertTrue(result.isPresent());
        assertEquals("apple", result.get());
    }

    @Test
    @DisplayName("测试findFirstValue-查找第一个元素值")
    public void testFindFirstValue() {
        List<String> list = Arrays.asList("apple", "banana", "apricot");
        String result = StreamUtils.findFirstValue(list, s -> s.startsWith("b"));

        assertEquals("banana", result);
    }

    @Test
    @DisplayName("测试findAny-查找任意元素")
    public void testFindAny() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> result = StreamUtils.findAny(list, num -> num > 3);

        assertTrue(result.isPresent());
        assertTrue(result.get() > 3);
    }

    @Test
    @DisplayName("测试findAnyValue-查找任意元素值")
    public void testFindAnyValue() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer result = StreamUtils.findAnyValue(list, num -> num > 3);

        assertNotNull(result);
        assertTrue(result > 3);
    }

    @Test
    @DisplayName("测试join-默认分隔符拼接")
    public void testJoinDefault() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        String result = StreamUtils.join(list, String::toUpperCase);

        assertEquals("APPLE,BANANA,CHERRY", result);
    }

    @Test
    @DisplayName("测试join-自定义分隔符拼接")
    public void testJoinCustomDelimiter() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        String result = StreamUtils.join(list, s -> s, " | ");

        assertEquals("apple | banana | cherry", result);
    }

    @Test
    @DisplayName("测试sorted-集合排序")
    public void testSorted() {
        List<Integer> list = Arrays.asList(5, 2, 8, 1, 9);
        List<Integer> result = StreamUtils.sorted(list, Integer::compareTo);

        assertEquals(Arrays.asList(1, 2, 5, 8, 9), result);
    }

    @Test
    @DisplayName("测试toIdentityMap-转换为Map(值不变)")
    public void testToIdentityMap() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        Map<Integer, String> result = StreamUtils.toIdentityMap(list, String::length);
        log.info("result: {}", result);
        assertEquals(2, result.size());
        assertEquals("apple", result.get(5));
        assertEquals("banana", result.get(6));
    }

    @Test
    @DisplayName("测试toMap-转换为Map")
    public void testToMap() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        Map<String, Integer> result = StreamUtils.toMap(list, s -> s, String::length);

        assertEquals(3, result.size());
        assertEquals(5, result.get("apple"));
        assertEquals(6, result.get("banana"));
        assertEquals(6, result.get("cherry"));
    }

    @Test
    @DisplayName("测试toMap-从Map转换")
    public void testToMapFromMap() {
        Map<String, Integer> sourceMap = new HashMap<>();
        sourceMap.put("a", 1);
        sourceMap.put("b", 2);
        sourceMap.put("c", 3);

        Map<String, Integer> result = StreamUtils.toMap(sourceMap, (k, v) -> v * 2);

        assertEquals(3, result.size());
        assertEquals(2, result.get("a"));
        assertEquals(4, result.get("b"));
        assertEquals(6, result.get("c"));
    }

    @Test
    @DisplayName("测试groupByKey-按key分组")
    public void testGroupByKey() {
        List<String> list = Arrays.asList("apple", "apricot", "banana", "blueberry", "cherry");
        Map<Character, List<String>> result = StreamUtils.groupByKey(list, s -> s.charAt(0));

        assertEquals(3, result.size());
        assertEquals(2, result.get('a').size());
        assertEquals(2, result.get('b').size());
        assertEquals(1, result.get('c').size());
    }

    @Test
    @DisplayName("测试groupBy2Key-按两个key分组")
    public void testGroupBy2Key() {
        List<String> list = Arrays.asList("apple", "apricot", "banana", "blueberry");
        Map<Character, Map<Integer, List<String>>> result =
            StreamUtils.groupBy2Key(list, s -> s.charAt(0), String::length);

        assertEquals(2, result.size());
        assertTrue(result.containsKey('a'));
        assertTrue(result.containsKey('b'));
        assertEquals(2, result.get('a').size()); // apple(5), apricot(7)
    }

    @Test
    @DisplayName("测试group2Map-按两个key分组为Map")
    public void testGroup2Map() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        Map<Character, Map<Integer, String>> result =
            StreamUtils.group2Map(list, s -> s.charAt(0), String::length);

        assertEquals(3, result.size());
        assertEquals("apple", result.get('a').get(5));
        assertEquals("banana", result.get('b').get(6));
        assertEquals("cherry", result.get('c').get(6));
    }

    @Test
    @DisplayName("测试toList-转换为List")
    public void testToList() {
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        List<Integer> result = StreamUtils.toList(list, String::length);

        assertEquals(3, result.size());
        assertTrue(result.contains(5));
        assertTrue(result.contains(6));
    }

    @Test
    @DisplayName("测试toSet-转换为Set")
    public void testToSet() {
        List<String> list = Arrays.asList("apple", "banana", "cherry", "avocado");
        Set<Integer> result = StreamUtils.toSet(list, String::length);

        // cherry和banana长度都是6,avocado长度7,apple长度5,所以Set中只有3个元素
        assertTrue(result.size() >= 2); // 至少有5和6
        assertTrue(result.contains(5));
        assertTrue(result.contains(6));
    }

    @Test
    @DisplayName("测试merge-合并两个Map")
    public void testMerge() {
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("a", 1);
        map1.put("b", 2);

        Map<String, Integer> map2 = new HashMap<>();
        map2.put("b", 3);
        map2.put("c", 4);

        Map<String, Integer> result = StreamUtils.merge(map1, map2, (v1, v2) -> {
            if (v1 == null) return v2;
            if (v2 == null) return v1;
            return v1 + v2;
        });

        assertEquals(3, result.size());
        assertEquals(1, result.get("a")); // 只在map1
        assertEquals(5, result.get("b")); // 1 + 3 = 5
        assertEquals(4, result.get("c")); // 只在map2
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 1000L; // 1秒
    }
}
