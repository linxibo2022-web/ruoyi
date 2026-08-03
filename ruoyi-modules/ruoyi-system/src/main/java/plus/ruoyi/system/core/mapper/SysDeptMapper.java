package plus.ruoyi.system.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.system.core.domain.SysDept;

import java.util.List;

/**
 * 部门管理 数据层
 *
 * @author Lion Li
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 统计指定部门ID的部门数量
     *
     * @param deptId 部门ID
     * @return 该部门ID的部门数量
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    long countDeptById(Long deptId);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    @DataPermission({
            @DataColumn(key = "deptName", value = "d.dept_id")
    })
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);
}
