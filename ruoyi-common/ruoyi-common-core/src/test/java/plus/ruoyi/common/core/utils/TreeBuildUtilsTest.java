package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.Data;
import cn.hutool.core.lang.tree.Tree;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TreeBuildUtils树形结构工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("TreeBuildUtils树形结构工具类测试")
public class TreeBuildUtilsTest extends BaseUnitTest {

    /**
     * 测试用部门节点
     */
    @Data
    static class Dept {
        private Long deptId;
        private Long parentId;
        private String deptName;
    }

    @Test
    @DisplayName("测试build-构建树形结构")
    public void testBuild() {
        // 准备测试数据:部门树
        List<Dept> list = new ArrayList<>();

        // 根节点
        Dept root = new Dept();
        root.setDeptId(1L);
        root.setParentId(0L);
        root.setDeptName("总公司");
        list.add(root);

        // 二级节点
        Dept dept1 = new Dept();
        dept1.setDeptId(2L);
        dept1.setParentId(1L);
        dept1.setDeptName("研发部");
        list.add(dept1);

        Dept dept2 = new Dept();
        dept2.setDeptId(3L);
        dept2.setParentId(1L);
        dept2.setDeptName("市场部");
        list.add(dept2);

        // 三级节点
        Dept dept11 = new Dept();
        dept11.setDeptId(4L);
        dept11.setParentId(2L);
        dept11.setDeptName("后端组");
        list.add(dept11);

        Dept dept12 = new Dept();
        dept12.setDeptId(5L);
        dept12.setParentId(2L);
        dept12.setDeptName("前端组");
        list.add(dept12);

        // 构建树 - 使用HuTool的NodeParser
        List<Tree<Long>> tree = TreeBuildUtils.build(list, 0L, (dept, treeNode) -> {
            treeNode.setId(dept.getDeptId());
            treeNode.setParentId(dept.getParentId());
            treeNode.setName(dept.getDeptName());
        });

        // 验证树结构
        assertNotNull(tree);
        assertEquals(1, tree.size(), "根节点应该只有1个");

        Tree<Long> rootNode = tree.get(0);
        assertEquals("总公司", rootNode.getName());
        assertEquals(2, rootNode.getChildren().size(), "总公司应该有2个子部门");

        Tree<Long> rdDept = rootNode.getChildren().get(0);
        assertEquals("研发部", rdDept.getName());
        assertEquals(2, rdDept.getChildren().size(), "研发部应该有2个子组");

        assertEquals("后端组", rdDept.getChildren().get(0).getName());
        assertEquals("前端组", rdDept.getChildren().get(1).getName());
    }

    @Test
    @DisplayName("测试build-空列表应返回空树")
    public void testBuildWithEmptyList() {
        List<Tree<Long>> tree = TreeBuildUtils.build(new ArrayList<Dept>(), 0L, (dept, treeNode) -> {
            treeNode.setId(dept.getDeptId());
            treeNode.setParentId(dept.getParentId());
            treeNode.setName(dept.getDeptName());
        });

        assertNotNull(tree);
        assertTrue(tree.isEmpty(), "空列表应该返回空树");
    }

    @Test
    @DisplayName("测试build-只有根节点")
    public void testBuildWithOnlyRoot() {
        List<Dept> list = new ArrayList<>();

        Dept root = new Dept();
        root.setDeptId(1L);
        root.setParentId(0L);
        root.setDeptName("根节点");
        list.add(root);

        List<Tree<Long>> tree = TreeBuildUtils.build(list, 0L, (dept, treeNode) -> {
            treeNode.setId(dept.getDeptId());
            treeNode.setParentId(dept.getParentId());
            treeNode.setName(dept.getDeptName());
        });

        assertEquals(1, tree.size());
        assertEquals("根节点", tree.get(0).getName());
        assertTrue(!tree.get(0).hasChild(), "只有根节点时应该没有子节点");
    }

    @Test
    @DisplayName("测试build-多个根节点")
    public void testBuildWithMultipleRoots() {
        List<Dept> list = new ArrayList<>();

        // 第一棵树
        Dept root1 = new Dept();
        root1.setDeptId(1L);
        root1.setParentId(0L);
        root1.setDeptName("树1");
        list.add(root1);

        Dept child1 = new Dept();
        child1.setDeptId(2L);
        child1.setParentId(1L);
        child1.setDeptName("树1-子节点");
        list.add(child1);

        // 第二棵树
        Dept root2 = new Dept();
        root2.setDeptId(3L);
        root2.setParentId(0L);
        root2.setDeptName("树2");
        list.add(root2);

        List<Tree<Long>> tree = TreeBuildUtils.build(list, 0L, (dept, treeNode) -> {
            treeNode.setId(dept.getDeptId());
            treeNode.setParentId(dept.getParentId());
            treeNode.setName(dept.getDeptName());
        });

        assertEquals(2, tree.size(), "应该有2棵树");
        assertEquals("树1", tree.get(0).getName());
        assertEquals("树2", tree.get(1).getName());
        assertEquals(1, tree.get(0).getChildren().size(), "树1应该有1个子节点");
    }
}
