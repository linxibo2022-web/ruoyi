package plus.ruoyi.common.test.base;

import org.springframework.transaction.annotation.Transactional;

/**
 * Service测试基类 - 基于 Spring 测试 + 事务回滚
 * <p>
 * 提供事务自动回滚支持,用于测试Service层业务逻辑
 *
 * <p>特性:
 * <ul>
 *   <li>继承 BaseSpringTest (轻量级 Spring 容器)</li>
 *   <li>测试方法执行后自动回滚事务,不污染数据库</li>
 *   <li>可以进行真实数据库操作测试</li>
 *   <li>支持Mock外部依赖</li>
 *   <li>自动性能监控和测试目录管理</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * {@code @DisplayName("用户服务测试")}
 * public class UserServiceTest extends BaseServiceTest {
 *
 *     {@code @Autowired}
 *     private ISysUserService userService;
 *
 *     {@code @Test}
 *     {@code @DisplayName("测试添加用户")}
 *     public void testAddUser() {
 *         UserBo user = new UserBo();
 *         user.setUserName("test");
 *         user.setNickName("测试用户");
 *
 *         // 执行业务逻辑
 *         Long userId = userService.add(user);
 *
 *         // 断言
 *         assertNotNull(userId);
 *         assertTrue(userId > 0);
 *
 *         // 测试结束后自动回滚,数据不会真正保存
 *     }
 * }
 * </pre>
 *
 * @author 抓蛙师
 */
@Transactional // 测试方法执行后自动回滚
public abstract class BaseServiceTest extends BaseSpringTest {
    // 继承 BaseSpringTest 的所有功能
}
