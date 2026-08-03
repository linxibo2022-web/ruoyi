package plus.ruoyi.business.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.mall.domain.Order;

/**
 * 订单Mapper接口
 * 提供订单表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface OrderMapper extends BaseMapper<Order> {
}
