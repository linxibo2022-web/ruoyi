package plus.ruoyi.system.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.system.core.domain.SysPost;

import java.util.List;

/**
 * 岗位信息 数据层
 *
 * @author Lion Li
 */
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    List<SysPost> selectPostsByUserId(Long userId);
}
