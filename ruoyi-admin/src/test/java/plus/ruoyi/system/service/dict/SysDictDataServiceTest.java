package plus.ruoyi.system.service.dict;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;
import plus.ruoyi.system.dict.service.ISysDictDataService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysDictDataService 字典数据服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("字典数据服务测试")
public class SysDictDataServiceTest extends BaseServiceTest {

    @Autowired
    private ISysDictDataService dictDataService;

    @Test
    @DisplayName("测试list-查询字典数据列表")
    public void testList() {
        SysDictDataBo queryBo = new SysDictDataBo();
        List<SysDictDataVo> list = dictDataService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() >= 0, "字典数据列表不应为null");
    }

    @Test
    @DisplayName("测试page-分页查询字典数据")
    public void testPage() {
        SysDictDataBo queryBo = new SysDictDataBo();
        PageQuery pageQuery = new PageQuery(10, 1);

        PageResult<SysDictDataVo> result = dictDataService.page(queryBo, pageQuery);

        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 0, "分页总数不应为负数");
    }

    @Test
    @DisplayName("测试list-按字典类型查询")
    public void testListByDictType() {
        SysDictDataBo queryBo = new SysDictDataBo();
        queryBo.setDictType("sys_enable_status"); // 启用状态字典

        List<SysDictDataVo> list = dictDataService.list(queryBo);

        assertNotNull(list);
        if (list.size() > 0) {
            // 验证查询结果都是指定类型
            list.forEach(dict -> {
                assertEquals("sys_enable_status", dict.getDictType(),
                    "字典类型应该匹配查询条件");
            });
        }
    }

    @Test
    @DisplayName("测试list-按状态查询")
    public void testListByStatus() {
        SysDictDataBo queryBo = new SysDictDataBo();
        queryBo.setStatus("0"); // 正常状态

        List<SysDictDataVo> list = dictDataService.list(queryBo);

        assertNotNull(list);
        if (list.size() > 0) {
            // 验证所有字典数据状态都是正常
            list.forEach(dict -> {
                assertEquals("0", dict.getStatus(), "字典状态应该是正常");
            });
        }
    }

    @Test
    @DisplayName("测试get-查询字典数据详情")
    public void testGet() {
        // 先查询列表获取一个ID
        SysDictDataBo queryBo = new SysDictDataBo();
        List<SysDictDataVo> list = dictDataService.list(queryBo);

        if (list != null && list.size() > 0) {
            Long dictDataId = list.get(0).getDictDataId();

            // 查询详情
            SysDictDataVo dict = dictDataService.get(dictDataId);

            assertNotNull(dict);
            assertEquals(dictDataId, dict.getDictDataId());
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
