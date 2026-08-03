package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseSpringTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import lombok.Data;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MapstructUtils 对象转换工具测试
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("MapstructUtils对象转换工具测试")
public class MapstructUtilsTest extends BaseSpringTest {

    // ==================== 测试用实体类 ====================

    @Data
    @AutoMappers({
        @AutoMapper(target = TargetDto.class, reverseConvertGenerate = false)
    })
    static class SourceEntity {
        private Long id;
        private String name;
        private Integer age;
        private String email;
    }

    @Data
    static class TargetDto {
        private Long id;
        private String name;
        private Integer age;
        private String email;
    }

    // ==================== 测试方法 ====================

    @Test
    @DisplayName("测试convert-对象转换为目标类型")
    public void testConvertObjectToClass() {
        // 准备源对象
        SourceEntity source = new SourceEntity();
        source.setId(1L);
        source.setName("张三");
        source.setAge(25);
        source.setEmail("zhangsan@example.com");

        // 执行转换
        TargetDto target = MapstructUtils.convert(source, TargetDto.class);

        // 验证结果
        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getAge(), target.getAge());
        assertEquals(source.getEmail(), target.getEmail());
    }

    @Test
    @DisplayName("测试convert-源对象为null应返回null")
    public void testConvertNullSource() {
        SourceEntity nullSource = null;
        TargetDto target = MapstructUtils.convert(nullSource, TargetDto.class);
        assertNull(target, "源对象为null时应返回null");
    }

    @Test
    @DisplayName("测试convert-目标类型为null应返回null")
    public void testConvertNullTargetClass() {
        SourceEntity source = new SourceEntity();
        source.setId(1L);

        Object target = MapstructUtils.convert(source, (Class<?>) null);
        assertNull(target, "目标类型为null时应返回null");
    }

    @Test
    @DisplayName("测试convert-对象赋值到已存在对象")
    public void testConvertToExistingObject() {
        // 准备源对象
        SourceEntity source = new SourceEntity();
        source.setId(1L);
        source.setName("李四");
        source.setAge(30);

        // 准备已存在的目标对象
        TargetDto target = new TargetDto();
        target.setEmail("existing@example.com"); // 已有的字段

        // 执行转换(赋值到现有对象)
        TargetDto result = MapstructUtils.convert(source, target);

        // 验证结果
        assertNotNull(result);
        assertSame(target, result, "应该返回同一个对象");
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getName(), result.getName());
        assertEquals(source.getAge(), result.getAge());
        // email字段应该保留原值或被覆盖(取决于映射配置)
    }

    @Test
    @DisplayName("测试convert-集合转换")
    public void testConvertList() {
        // 准备源集合
        SourceEntity source1 = new SourceEntity();
        source1.setId(1L);
        source1.setName("张三");

        SourceEntity source2 = new SourceEntity();
        source2.setId(2L);
        source2.setName("李四");

        SourceEntity source3 = new SourceEntity();
        source3.setId(3L);
        source3.setName("王五");

        List<SourceEntity> sourceList = List.of(source1, source2, source3);

        // 执行转换
        List<TargetDto> targetList = MapstructUtils.convert(sourceList, TargetDto.class);

        // 验证结果
        assertNotNull(targetList);
        assertEquals(3, targetList.size());

        assertEquals(1L, targetList.get(0).getId());
        assertEquals("张三", targetList.get(0).getName());

        assertEquals(2L, targetList.get(1).getId());
        assertEquals("李四", targetList.get(1).getName());

        assertEquals(3L, targetList.get(2).getId());
        assertEquals("王五", targetList.get(2).getName());
    }

    @Test
    @DisplayName("测试convert-空集合应返回空集合")
    public void testConvertEmptyList() {
        List<SourceEntity> emptyList = List.of();

        List<TargetDto> targetList = MapstructUtils.convert(emptyList, TargetDto.class);

        assertNotNull(targetList, "空集合不应返回null");
        assertTrue(targetList.isEmpty(), "应该返回空集合");
    }

    @Test
    @DisplayName("测试convert-null集合应返回null")
    public void testConvertNullList() {
        List<TargetDto> targetList = MapstructUtils.convert((List<SourceEntity>) null, TargetDto.class);
        assertNull(targetList, "null集合应返回null");
    }

    @Test
    @DisplayName("测试convert-Map转对象")
    public void testConvertMapToObject() {
        // 准备Map数据
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "赵六");
        map.put("age", 28);
        map.put("email", "zhaoliu@example.com");

        // 执行转换
        TargetDto target = MapstructUtils.convert(map, TargetDto.class);

        // 验证结果
        assertNotNull(target);
        assertEquals(1L, target.getId());
        assertEquals("赵六", target.getName());
        assertEquals(28, target.getAge());
        assertEquals("zhaoliu@example.com", target.getEmail());
    }

    @Test
    @DisplayName("测试convert-空Map应返回null")
    public void testConvertEmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();

        TargetDto target = MapstructUtils.convert(emptyMap, TargetDto.class);
        assertNull(target, "空Map应返回null");
    }

    @Test
    @DisplayName("测试convert-null Map应返回null")
    public void testConvertNullMap() {
        TargetDto target = MapstructUtils.convert((Map<String, Object>) null, TargetDto.class);
        assertNull(target, "null Map应返回null");
    }

    @Test
    @DisplayName("测试convert-大量数据转换性能")
    public void testConvertPerformance() {
        // 准备大量数据
        List<SourceEntity> largeList = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            SourceEntity source = new SourceEntity();
            source.setId((long) i);
            source.setName("用户" + i);
            source.setAge(20 + (i % 50));
            largeList.add(source);
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 执行转换
        List<TargetDto> targetList = MapstructUtils.convert(largeList, TargetDto.class);

        // 记录结束时间
        long endTime = System.currentTimeMillis();

        // 验证结果
        assertNotNull(targetList);
        assertEquals(1000, targetList.size());

        // 性能验证(转换1000条数据应该在合理时间内完成)
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "1000条数据转换应在5秒内完成，实际耗时: " + duration + "ms");

        // 验证数据正确性
        assertEquals(0L, targetList.get(0).getId());
        assertEquals("用户0", targetList.get(0).getName());
        assertEquals(999L, targetList.get(999).getId());
        assertEquals("用户999", targetList.get(999).getName());
    }
}
