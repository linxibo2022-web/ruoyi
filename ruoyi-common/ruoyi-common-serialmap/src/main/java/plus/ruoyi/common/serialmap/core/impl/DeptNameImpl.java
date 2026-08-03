package plus.ruoyi.common.serialmap.core.impl;

import plus.ruoyi.common.core.service.DeptService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import lombok.AllArgsConstructor;

/**
 * 部门ID转名称转换器
 *
 * <p>支持单个ID或多个ID（逗号分隔）转换为部门名称
 *
 * @author 抓蛙师
 */
@SerialMapType(type = SerialMapConstant.DEPT_ID_TO_NAME)
public class DeptNameImpl implements SerialMapInterface<String> {

    private final DeptService deptService;

    /**
     * 构造方法
     */
    public DeptNameImpl(DeptService deptService) {
        this.deptService = deptService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof String ids) {
            return deptService.getDeptNameByIds(ids);
        } else if (key instanceof Long id) {
            return deptService.getDeptNameByIds(id.toString());
        }
        return null;
    }
}
