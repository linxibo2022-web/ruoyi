package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Payment;

/**
 * 支付配置Mapper接口
 * 提供支付配置表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface PaymentMapper extends BaseMapper<Payment> {

}
