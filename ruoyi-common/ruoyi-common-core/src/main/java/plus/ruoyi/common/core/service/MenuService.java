package plus.ruoyi.common.core.service;

/**
 * 通用 菜单服务
 * <p>提供菜单相关的通用服务方法，供其他模块调用</p>
 *
 * @author Lion Li
 */
public interface MenuService {

    /**
     * 根据权限字符串检查菜单是否存在
     * <p>用于代码生成器自动导入菜单时的去重判断</p>
     *
     * @param perms 权限字符串（如: base:ad:view）
     * @return 是否存在
     */
    boolean existsByPerms(String perms);

    /**
     * 根据权限字符串获取菜单名称
     * <p>用于权限校验失败时提供友好的错误提示</p>
     *
     * @param perms 权限字符串（如: base:ad:query）
     * @return 菜单名称，如果未找到则返回null
     */
    String getMenuNameByPerms(String perms);
}
