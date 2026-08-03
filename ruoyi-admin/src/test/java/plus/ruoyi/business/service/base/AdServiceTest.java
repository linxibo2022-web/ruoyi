package plus.ruoyi.business.service.base;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.common.test.base.TestDataBuilder;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.service.IAdService;
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
 * AdService 广告配置服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional // 关键!测试结束自动回滚
@DisplayName("广告配置服务测试")
public class AdServiceTest extends BaseServiceTest {

    @Autowired
    private IAdService adService;

    @Test
    @DisplayName("测试add-新增广告配置成功")
    public void testAdd() {
        // 准备测试数据
        AdBo ad = new AdBo();
        ad.setAdName("测试广告_" + System.currentTimeMillis());
        ad.setAdType("1"); // 广告类型(必填)
        ad.setPosition("home_top"); // 首页顶部
        ad.setImg("https://test.com/ad.jpg");
        ad.setDescription("测试广告描述");
        ad.setJumpPath("/pages/detail/detail");
        ad.setSortOrder(1L);
        ad.setStatus("0"); // 启用
        ad.setRemark("测试广告备注");

        // 执行新增
        Long adId = adService.add(ad);

        // 验证
        assertNotNull(adId, "广告ID不应为null");
        assertTrue(adId > 0, "广告ID应该大于0");

        // 验证可以查询到
        AdVo adVo = adService.get(adId);
        assertNotNull(adVo, "应该能查询到刚插入的广告");
        assertEquals(ad.getAdName(), adVo.getAdName());
        assertEquals(ad.getAdType(), adVo.getAdType());
        assertEquals(ad.getImg(), adVo.getImg());

        // ⚠️ 测试结束后,@Transactional会自动回滚
    }

    @Test
    @DisplayName("测试get-查询存在的广告")
    public void testGet() {
        // 1. 先插入一条测试数据
        AdBo ad = new AdBo();
        ad.setAdName("查询测试_" + System.currentTimeMillis());
        ad.setAdType("1");
        ad.setImg("https://test.com/ad.jpg");
        ad.setPosition("home_top");
        ad.setSortOrder(1L);
        ad.setStatus("0");

        Long adId = adService.add(ad);
        assertNotNull(adId);

        // 2. 查询
        AdVo adVo = adService.get(adId);

        // 3. 验证
        assertNotNull(adVo);
        assertEquals(adId, adVo.getId());
        assertEquals(ad.getAdName(), adVo.getAdName());

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试get-查询不存在的广告应返回null")
    public void testGetNotExists() {
        AdVo adVo = adService.get(999999L);
        assertNull(adVo, "不存在的广告应该返回null");
    }

    @Test
    @DisplayName("测试page-分页查询广告列表")
    public void testPage() {
        // 1. 先插入几条测试数据
        for (int i = 0; i < 3; i++) {
            AdBo ad = new AdBo();
            ad.setAdName("分页测试_" + i + "_" + System.currentTimeMillis());
            ad.setAdType("1");
            ad.setImg("https://test.com/ad" + i + ".jpg");
            ad.setPosition("home_top");
            ad.setSortOrder((long) i);
            ad.setStatus("0");

            adService.add(ad);
        }

        // 2. 执行分页查询
        AdBo queryBo = new AdBo();
        PageQuery pageQuery = new PageQuery(10, 1); // pageSize, pageNum

        PageResult<AdVo> result = adService.page(queryBo, pageQuery);

        // 3. 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 3, "应该至少有3条广告数据");

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试page-按广告名称模糊查询")
    public void testPageByAdName() {
        // 1. 插入测试数据
        String uniqueName = "搜索测试_" + System.currentTimeMillis();
        AdBo ad = new AdBo();
        ad.setAdName(uniqueName);
        ad.setAdType("1");
        ad.setImg("https://test.com/ad.jpg");
        ad.setPosition("home_top");
        ad.setSortOrder(1L);
        ad.setStatus("0");

        adService.add(ad);

        // 2. 按名称查询
        AdBo queryBo = new AdBo();
        queryBo.setAdName(uniqueName);

        PageQuery pageQuery = new PageQuery(10, 1);

        PageResult<AdVo> result = adService.page(queryBo, pageQuery);

        // 3. 验证
        assertNotNull(result);
        assertTrue(result.getTotal() > 0, "应该能查到测试广告");

        boolean found = result.getRecords().stream()
            .anyMatch(a -> uniqueName.equals(a.getAdName()));
        assertTrue(found, "查询结果应该包含测试广告");

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试update-更新广告配置")
    public void testUpdate() {
        // 1. 插入测试数据
        AdBo ad = new AdBo();
        ad.setAdName("更新测试_" + System.currentTimeMillis());
        ad.setAdType("1");
        ad.setDescription("原始描述");
        ad.setImg("https://test.com/ad.jpg");
        ad.setPosition("home_top");
        ad.setSortOrder(1L);
        ad.setStatus("0");

        Long adId = adService.add(ad);
        assertNotNull(adId);

        // 2. 更新数据
        ad.setId(adId);
        ad.setDescription("更新后的描述");
        ad.setImg("https://test.com/new_ad.jpg");
        ad.setStatus("1"); // 停用

        int updateResult = adService.update(ad);
        assertTrue(updateResult > 0, "更新应该成功");

        // 3. 验证更新是否生效
        AdVo updated = adService.get(adId);
        assertNotNull(updated);
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals("https://test.com/new_ad.jpg", updated.getImg());
        assertEquals("1", updated.getStatus());

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试batchDelete-批量删除广告")
    public void testBatchDelete() {
        // 1. 插入多条测试数据
        Long id1 = adService.add(createTestAd("批量删除1"));
        Long id2 = adService.add(createTestAd("批量删除2"));
        Long id3 = adService.add(createTestAd("批量删除3"));

        // 2. 验证数据存在
        assertNotNull(adService.get(id1));
        assertNotNull(adService.get(id2));
        assertNotNull(adService.get(id3));

        // 3. 批量删除
        List<Long> ids = List.of(id1, id2, id3);
        int deleteResult = adService.batchDelete(ids);
        assertTrue(deleteResult > 0, "批量删除应该成功");

        // 4. 验证已删除
        assertNull(adService.get(id1), "id1应该已被删除");
        assertNull(adService.get(id2), "id2应该已被删除");
        assertNull(adService.get(id3), "id3应该已被删除");

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试changeAdStatus-修改广告状态")
    public void testChangeAdStatus() {
        // 1. 插入测试数据(启用状态)
        AdBo ad = createTestAd("状态测试");
        ad.setStatus("0"); // 启用
        Long adId = adService.add(ad);

        // 2. 修改为停用
        ad.setId(adId);
        ad.setStatus("1"); // 停用
        int result = adService.update(ad);
        assertTrue(result > 0);

        // 3. 验证状态已更新
        AdVo updated = adService.get(adId);
        assertEquals("1", updated.getStatus());

        // 4. 再次修改回启用
        ad.setStatus("0");
        result = adService.update(ad);
        assertTrue(result > 0);

        updated = adService.get(adId);
        assertEquals("0", updated.getStatus());

        // ⚠️ 测试结束自动回滚
    }

    /**
     * 创建测试广告对象
     */
    private AdBo createTestAd(String prefix) {
        AdBo ad = new AdBo();
        ad.setAdName(prefix + "_" + System.currentTimeMillis());
        ad.setAdType("1");
        ad.setDescription(prefix + "描述");
        ad.setImg("https://test.com/" + prefix + ".jpg");
        ad.setPosition("home_top");
        ad.setSortOrder(1L);
        ad.setStatus("0");
        return ad;
    }
}
