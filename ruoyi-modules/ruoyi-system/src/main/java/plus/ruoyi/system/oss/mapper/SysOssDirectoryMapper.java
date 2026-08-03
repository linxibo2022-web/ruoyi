package plus.ruoyi.system.oss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import plus.ruoyi.system.oss.domain.SysOssDirectory;

import java.util.List;


/**
 * OSS目录Mapper接口
 * 提供OSS目录表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
public interface SysOssDirectoryMapper extends BaseMapper<SysOssDirectory> {

    /**
     * 根据祖先ID查询所有子目录
     *
     * @param ancestorId 祖先ID
     * @return 子目录列表
     */
    List<SysOssDirectory> selectChildrenByAncestors(@Param("ancestorId") String ancestorId);
}
