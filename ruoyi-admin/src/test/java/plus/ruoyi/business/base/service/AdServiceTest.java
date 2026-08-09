// 广告配置 Service 层单元测试
package plus.ruoyi.business.base.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.test.base.BaseServiceTest;

import static org.assertj.core.api.Assertions.*;

/**
 * 广告配置服务测试
 * <p>
 * 继承 BaseServiceTest，测试结束后自动回滚事务，不污染数据库
 *
 * @author 抓蛙师
 */
@Slf4j
@Tag("dev")
@SpringBootTest
@DisplayName("广告配置 Service 测试")
class AdServiceTest extends BaseServiceTest {

    @Autowired
    private IAdService adService;

    @Test
    @DisplayName("新增广告 — 正常提交")
    void testAdd() {
        // Arrange: 准备测试数据
        AdBo bo = new AdBo();
        bo.setAdName("E2E测试广告");
        bo.setAdType("1");
        bo.setStatus("1");

        // Act: 执行操作
        Long id = adService.add(bo);

        // Assert: 验证结果
        assertThat(id).isNotNull();
        assertThat(id).isGreaterThan(0);

        // 验证数据确实写入了
        var vo = adService.get(id);
        assertThat(vo).isNotNull();
        assertThat(vo.getAdName()).isEqualTo("E2E测试广告");

        log.info("✅ 新增广告测试通过, id={}", id);
    }

    @Test
    @DisplayName("修改广告 — ID为空抛异常")
    void testUpdateWithoutId() {
        AdBo bo = new AdBo();
        bo.setAdName("测试广告");
        // 不设置 id → 应抛出 ServiceException

        assertThatThrownBy(() -> adService.update(bo))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("广告配置ID不能为空");

        log.info("✅ ID为空校验测试通过");
    }

    @Test
    @DisplayName("批量删除 — ID集合为空抛异常")
    void testBatchDeleteWithEmptyIds() {
        assertThatThrownBy(() -> adService.batchDelete(null))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("ID集合不能为空");

        log.info("✅ 空集合校验测试通过");
    }
}
