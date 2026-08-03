package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Bind;

/**
 * 账号绑定Mapper接口
 * 提供账号绑定表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface BindMapper extends BaseMapper<Bind> {

}
