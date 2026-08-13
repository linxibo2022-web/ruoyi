package plus.ruoyi.system.service.config;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.system.config.domain.bo.SysConfigBo;
import plus.ruoyi.system.config.domain.vo.SysConfigVo;
import plus.ruoyi.system.config.service.ISysConfigService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysConfigService 系统配置服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("系统配置服务测试")
@Tag("dev")
public class SysConfigServiceTest extends BaseServiceTest {

    @Autowired
    private ISysConfigService configService;

    @Test
    @DisplayName("测试list-查询配置列表")
    public void testList() {
        SysConfigBo queryBo = new SysConfigBo();
        List<SysConfigVo> list = configService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() >= 0, "配置列表不应为null");
    }

    @Test
    @DisplayName("测试page-分页查询配置")
    public void testPage() {
        SysConfigBo queryBo = new SysConfigBo();
        PageQuery pageQuery = new PageQuery(10, 1);

        PageResult<SysConfigVo> result = configService.page(queryBo, pageQuery);

        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 0, "分页总数不应为负数");
    }

    @Test
    @DisplayName("测试list-按配置名称查询")
    public void testListByConfigName() {
        SysConfigBo queryBo = new SysConfigBo();
        queryBo.setConfigName("系统"); // 查找包含"系统"的配置

        List<SysConfigVo> list = configService.list(queryBo);

        assertNotNull(list);
        if (list.size() > 0) {
            // 验证查询结果包含关键字
            boolean hasKeyword = list.stream()
                .anyMatch(config -> config.getConfigName().contains("系统"));
            assertTrue(hasKeyword, "查询结果应该包含'系统'相关配置");
        }
    }

    @Test
    @DisplayName("测试get-查询配置详情")
    public void testGet() {
        // 先查询列表获取一个ID
        SysConfigBo queryBo = new SysConfigBo();
        List<SysConfigVo> list = configService.list(queryBo);

        if (list != null && list.size() > 0) {
            Long configId = list.get(0).getConfigId();

            // 查询详情
            SysConfigVo config = configService.get(configId);

            assertNotNull(config);
            assertEquals(configId, config.getConfigId());
        }
    }

    @Test
    @DisplayName("测试list-按配置类型查询")
    public void testListByConfigType() {
        SysConfigBo queryBo = new SysConfigBo();
        queryBo.setConfigType("Y"); // 系统内置

        List<SysConfigVo> list = configService.list(queryBo);

        assertNotNull(list);
        if (list.size() > 0) {
            // 验证配置类型
            list.forEach(config -> {
                assertEquals("Y", config.getConfigType(), "配置类型应该是系统内置");
            });
        }
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 2000L; // 2秒
    }
}
