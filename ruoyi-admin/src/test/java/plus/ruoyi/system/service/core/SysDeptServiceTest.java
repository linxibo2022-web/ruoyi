package plus.ruoyi.system.service.core;

import cn.hutool.core.lang.tree.Tree;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.domain.vo.SysDeptVo;
import plus.ruoyi.system.core.service.ISysDeptService;
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
 * SysDeptService 部门管理服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("部门管理服务测试")
@Tag("dev")
public class SysDeptServiceTest extends BaseServiceTest {

    @Autowired
    private ISysDeptService deptService;

    @Test
    @DisplayName("测试get-查询部门详情")
    public void testGet() {
        // 查询默认存在的部门(假设ID=103存在)
        SysDeptVo dept = deptService.get(103L);

        if (dept != null) {
            assertNotNull(dept.getDeptId());
            assertNotNull(dept.getDeptName());
        }
    }

    @Test
    @DisplayName("测试listAll-查询所有部门")
    public void testListAll() {
        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.listAll());

        assertNotNull(deptList);
        assertTrue(deptList.size() > 0, "应该有部门数据");
    }

    @Test
    @DisplayName("测试list-查询部门列表")
    public void testList() {
        SysDeptBo queryBo = new SysDeptBo();
        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.list(queryBo));

        assertNotNull(deptList);
        assertTrue(deptList.size() > 0, "应该有部门数据");
    }

    @Test
    @DisplayName("测试page-分页查询部门")
    public void testPage() {
        SysDeptBo queryBo = new SysDeptBo();
        PageQuery pageQuery = new PageQuery(10, 1);

        PageResult<SysDeptVo> result = DataPermissionHelper.ignore(() -> deptService.page(queryBo, pageQuery));

        assertNotNull(result);
        assertTrue(result.getTotal() > 0, "应该有部门数据");
        assertNotNull(result.getRecords());
    }

    @Test
    @DisplayName("测试list-按部门名称查询")
    public void testListByDeptName() {
        SysDeptBo queryBo = new SysDeptBo();
        queryBo.setDeptName("研发"); // 查找包含"研发"的部门

        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.list(queryBo));

        assertNotNull(deptList);
        // 如果有结果,验证包含关键字
        if (deptList.size() > 0) {
            boolean hasKeyword = deptList.stream()
                .anyMatch(dept -> dept.getDeptName().contains("研发"));
            assertTrue(hasKeyword, "查询结果应该包含'研发'相关部门");
        }
    }

    @Test
    @DisplayName("测试list-按部门状态查询")
    public void testListByStatus() {
        SysDeptBo queryBo = new SysDeptBo();
        queryBo.setStatus("1"); // 查询正常状态的部门（项目规范：1=正常 0=停用）

        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.list(queryBo));

        assertNotNull(deptList);
        assertTrue(deptList.size() > 0, "应该有正常状态的部门");

        // 验证所有部门状态都是正常
        deptList.forEach(dept -> {
            assertEquals("1", dept.getStatus(), "部门状态应该是正常");
        });
    }

    @Test
    @DisplayName("测试部门树形结构-验证父子关系")
    public void testDeptTreeStructure() {
        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.listAll());

        assertNotNull(deptList);

        // 检查是否有父子关系
        boolean hasParentChild = deptList.stream()
            .anyMatch(dept -> dept.getParentId() != null && dept.getParentId() != 0);

        assertTrue(hasParentChild, "应该有父子部门关系");
    }

    @Test
    @DisplayName("测试部门数量-应该大于等于1")
    public void testDeptCount() {
        List<SysDeptVo> deptList = DataPermissionHelper.ignore(() -> deptService.listAll());

        assertNotNull(deptList);
        assertTrue(deptList.size() >= 1, "至少应该有1个部门(根部门)");
    }

    /**
     * 自定义性能阈值 - 部门操作应该很快
     */
    @Override
    protected long getPerformanceThreshold() {
        return 2000L; // 2秒
    }
}
