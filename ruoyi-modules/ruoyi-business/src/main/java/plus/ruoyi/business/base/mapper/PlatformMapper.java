package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Platform;

/**
 * 平台配置Mapper接口
 * 提供平台配置表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface PlatformMapper extends BaseMapper<Platform> {

}
