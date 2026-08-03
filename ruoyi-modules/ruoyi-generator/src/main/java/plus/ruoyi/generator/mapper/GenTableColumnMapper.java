package plus.ruoyi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.generator.domain.GenTableColumn;

/**
 * 代码生成表字段Mapper接口
 *
 * @author Lion Li
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {

}
