package plus.ruoyi.system.core.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author Lion Li
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 分页查询角色(带联表查询，用于数据权限)
     *
     * @param page 分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    IPage<SysRole> selectRolePageWithJoin(@Param("page") IPage<SysRole> page, @Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper);

    /**
     * 查询角色列表(带联表查询，用于数据权限)
     *
     * @param queryWrapper 查询条件
     * @return 角色列表
     */
    List<SysRole> selectRoleListWithJoin(@Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper);

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 对应的角色信息
     */
    SysRole selectRoleById(Long roleId);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);
}
