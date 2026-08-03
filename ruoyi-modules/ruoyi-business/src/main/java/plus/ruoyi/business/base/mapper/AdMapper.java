package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Ad;

/**
 * 广告配置Mapper接口
 * 提供广告配置表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface AdMapper extends BaseMapper<Ad> {

}
