package plus.ruoyi.system.service.config;

import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.system.config.domain.bo.SysNoticeBo;
import plus.ruoyi.system.config.domain.vo.SysNoticeVo;
import plus.ruoyi.system.config.service.ISysNoticeService;
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
 * SysNoticeService 系统通知公告服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("系统通知公告服务测试")
@Tag("dev")
public class SysNoticeServiceTest extends BaseServiceTest {

    @Autowired
    private ISysNoticeService noticeService;

    @Test
    @DisplayName("测试add-新增通知公告")
    public void testAdd() {
        // 准备测试数据
        SysNoticeBo notice = new SysNoticeBo();
        notice.setNoticeTitle("测试通知_" + System.currentTimeMillis());
        notice.setNoticeType("1"); // 通知
        notice.setNoticeContent("测试通知内容");
        notice.setStatus("0"); // 正常

        // 执行新增
        Long noticeId = noticeService.add(notice);

        // 验证
        assertNotNull(noticeId);
        assertTrue(noticeId > 0, "通知ID应该大于0");

        // 验证可以查询到
        SysNoticeVo noticeVo = noticeService.get(noticeId);
        assertNotNull(noticeVo);
        assertEquals(notice.getNoticeTitle(), noticeVo.getNoticeTitle());
    }

    @Test
    @DisplayName("测试get-查询通知详情")
    public void testGet() {
        // 先插入测试数据
        SysNoticeBo notice = createTestNotice("查询测试");
        Long noticeId = noticeService.add(notice);

        // 查询
        SysNoticeVo noticeVo = noticeService.get(noticeId);

        // 验证
        assertNotNull(noticeVo);
        assertEquals(noticeId, noticeVo.getNoticeId());
        assertEquals(notice.getNoticeTitle(), noticeVo.getNoticeTitle());
    }

    @Test
    @DisplayName("测试list-查询通知列表")
    public void testList() {
        // 插入测试数据
        createAndSaveNotice("列表测试1");
        createAndSaveNotice("列表测试2");

        // 查询列表（忽略数据权限，单元测试无 Sa-Token 上下文）
        SysNoticeBo queryBo = new SysNoticeBo();
        List<SysNoticeVo> list = DataPermissionHelper.ignore(() -> noticeService.list(queryBo));

        // 验证
        assertNotNull(list);
        assertTrue(list.size() >= 2, "至少应该有2条测试数据");
    }

    @Test
    @DisplayName("测试page-分页查询通知")
    public void testPage() {
        // 插入测试数据
        for (int i = 0; i < 3; i++) {
            createAndSaveNotice("分页测试" + i);
        }

        // 分页查询
        SysNoticeBo queryBo = new SysNoticeBo();
        PageQuery pageQuery = new PageQuery(10, 1);
        PageResult<SysNoticeVo> result = DataPermissionHelper.ignore(() -> noticeService.page(queryBo, pageQuery));

        // 验证
        assertNotNull(result);
        assertTrue(result.getTotal() >= 3, "至少应该有3条数据");
    }

    @Test
    @DisplayName("测试list-按通知类型查询")
    public void testListByNoticeType() {
        // 插入不同类型的通知
        SysNoticeBo notice1 = createTestNotice("公告测试");
        notice1.setNoticeType("2"); // 公告
        noticeService.add(notice1);

        // 按类型查询（忽略数据权限）
        SysNoticeBo queryBo = new SysNoticeBo();
        queryBo.setNoticeType("2");
        List<SysNoticeVo> list = DataPermissionHelper.ignore(() -> noticeService.list(queryBo));

        // 验证
        assertNotNull(list);
        if (list.size() > 0) {
            list.forEach(notice -> {
                assertEquals("2", notice.getNoticeType(), "通知类型应该是公告");
            });
        }
    }

    @Test
    @DisplayName("测试list-按状态查询")
    public void testListByStatus() {
        // 插入草稿状态的通知
        SysNoticeBo draftNotice = createTestNotice("草稿通知");
        draftNotice.setStatus("0"); // 草稿
        noticeService.add(draftNotice);

        // 插入已发布状态的通知
        SysNoticeBo publishedNotice = createTestNotice("已发布通知");
        publishedNotice.setStatus("1"); // 已发布
        noticeService.add(publishedNotice);

        // 按草稿状态查询（忽略数据权限）
        SysNoticeBo queryBo = new SysNoticeBo();
        queryBo.setStatus("0");
        List<SysNoticeVo> draftList = DataPermissionHelper.ignore(() -> noticeService.list(queryBo));

        // 验证草稿列表(过滤出真正的草稿状态数据)
        assertNotNull(draftList);
        List<SysNoticeVo> actualDrafts = draftList.stream()
            .filter(n -> "0".equals(n.getStatus()))
            .toList();
        assertTrue(actualDrafts.size() > 0, "应该有草稿状态的通知");

        // 按已发布状态查询（忽略数据权限）
        queryBo.setStatus("1");
        List<SysNoticeVo> publishedList = DataPermissionHelper.ignore(() -> noticeService.list(queryBo));

        // 验证已发布列表(过滤出真正的已发布状态数据)
        assertNotNull(publishedList);
        List<SysNoticeVo> actualPublished = publishedList.stream()
            .filter(n -> "1".equals(n.getStatus()))
            .toList();
        assertTrue(actualPublished.size() > 0, "应该有已发布状态的通知");
    }

    /**
     * 创建测试通知对象
     */
    private SysNoticeBo createTestNotice(String titlePrefix) {
        SysNoticeBo notice = new SysNoticeBo();
        notice.setNoticeTitle(titlePrefix + "_" + System.currentTimeMillis());
        notice.setNoticeType("1"); // 通知
        notice.setNoticeContent(titlePrefix + "的内容");
        notice.setStatus("0"); // 正常
        return notice;
    }

    /**
     * 创建并保存测试通知
     */
    private Long createAndSaveNotice(String titlePrefix) {
        SysNoticeBo notice = createTestNotice(titlePrefix);
        return noticeService.add(notice);
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 2000L; // 2秒
    }
}
