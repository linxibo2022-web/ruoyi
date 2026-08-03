package plus.ruoyi.common.sensitive.core;

/**
 * 数据脱敏服务接口
 *
 * <p>定义数据脱敏的权限判断逻辑，用于决定是否对敏感数据进行脱敏处理。
 *
 * <p>默认行为：
 * <ul>
 *   <li>管理员角色默认不进行脱敏，可查看原始数据</li>
 *   <li>普通用户根据角色和权限配置进行脱敏判断</li>
 * </ul>
 *
 * <p>自定义实现：
 * <pre>
 * {@code @Service}
 * public class CustomSensitiveService implements SensitiveService {
 *     {@code @Override}
 *     public boolean isSensitive(String[] roleKey, String[] perms) {
 *         // 获取当前用户信息
 *         LoginUser user = LoginHelper.getLoginUser();
 *         if (user == null) {
 *             return true; // 未登录用户全部脱敏
 *         }
 *
 *         // 超级管理员不脱敏
 *         if (LoginHelper.isSuperAdmin()) {
 *             return false;
 *         }
 *
 *         // 检查角色权限
 *         return !hasRoleOrPermission(user, roleKey, perms);
 *     }
 * }
 * </pre>
 *
 * <p>注意：需要根据具体业务场景重写实现，以满足不同的权限控制需求。
 *
 * @author Lion Li
 * @version 3.6.0
 */
public interface SensitiveService {

    /**
     * 判断是否需要进行数据脱敏
     *
     * <p>根据用户的角色和权限信息，判断当前用户是否需要对敏感数据进行脱敏处理。
     *
     * @param roleKey 允许查看原始数据的角色标识数组
     * @param perms 允许查看原始数据的权限标识数组
     * @return true表示需要脱敏，false表示不需要脱敏(显示原始数据)
     */
    boolean isSensitive(String[] roleKey, String[] perms);

}
