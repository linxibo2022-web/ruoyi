package plus.ruoyi.common.mybatis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;

/**
 * 字典枚举-数据权限类型
 * <p>
 * 支持使用 SpEL 模板表达式定义 SQL 查询条件
 * 内置数据：
 * - {@code user}: 当前登录用户信息，参考 {@link LoginUser}
 * 内置服务：
 * - {@code sdss}: 系统数据权限服务，参考 ISysDataScopeService
 * 如需扩展数据，可以通过 {@link DataPermissionHelper} 进行操作
 * 如需扩展服务，可以通过 ISysDataScopeService 自行编写
 * </p>
 *
 * @author Lion Li
 * @version 3.5.0
 */
@Getter
@AllArgsConstructor
public enum DataScopeType {

    /**
     * 全部数据权限
     */
    ALL("1", "全部数据权限", "", ""),

    /**
     * 自定数据权限
     */
    CUSTOM("2", "自定义数据权限", " #{#deptName} IN ( #{@sdss.getRoleCustom( #user.roleId )} ) ", " 1 = 0 "),

    /**
     * 部门数据权限
     */
    DEPT("3", "部门数据权限", " #{#deptName} = #{#user.deptId} ", " 1 = 0 "),

    /**
     * 部门及以下数据权限
     */
    DEPT_AND_CHILD("4", "部门及以下数据权限", " #{#deptName} IN ( #{@sdss.getDeptAndChild( #user.deptId )} )", " 1 = 0 "),

    /**
     * 仅本人数据权限
     */
    SELF("5", "仅本人数据权限", " #{#userName} = #{#user.userId} ", " 1 = 0 "),

    /**
     * 部门及以下或本人数据权限
     */
    DEPT_AND_CHILD_OR_SELF("6", "部门及以下或本人数据", " #{#deptName} IN ( #{@sdss.getDeptAndChild( #user.deptId )} ) OR #{#userName} = #{#user.userId} ", " 1 = 0 ");

    /**
     * 数据权限字典类型
     */
    public static final String DICT_TYPE = "sys_data_scope";

    /**
     * 字典值
     */
    private final String value;

    /**
     * 字典标签
     */
    private final String label;

    /**
     * SpEL 模板表达式,用于构建 SQL 查询条件
     */
    private final String sqlTemplate;

    /**
     * 如果不满足 {@code sqlTemplate} 的条件,则使用此默认 SQL 表达式
     */
    private final String elseSql;

    /**
     * 根据字典值获取枚举
     */
    public static DataScopeType getByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (DataScopeType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DataScopeType getByLabel(String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        for (DataScopeType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return null;
    }
}
