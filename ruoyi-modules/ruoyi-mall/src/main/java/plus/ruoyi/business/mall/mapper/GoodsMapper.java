package plus.ruoyi.business.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.mall.domain.Goods;

/**
 * 商品Mapper接口
 * 提供商品表的CRUD和数据权限控制功能
 *
 * @author 抓蛙师
 */
//@DataPermission({
//    @DataColumn(key = "deptName", value = "create_dept"),
//    @DataColumn(key = "userName", value = "create_by")
//})
public interface GoodsMapper extends BaseMapper<Goods> {

}
