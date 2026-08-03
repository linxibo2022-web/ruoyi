package plus.ruoyi.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试登录助手 - 用于测试时提供Token
 * <p>
 * 自动调用开发环境的 /getToken 接口获取token，无需手动配置
 * <p>
 * 使用说明:
 * <ol>
 *   <li>确保应用运行在开发环境(dev profile)</li>
 *   <li>直接在测试中调用 loginAsSuperAdmin() 或 loginAsUser(userId)</li>
 *   <li>自动获取并返回 Bearer token</li>
 * </ol>
 *
 * @author 抓蛙师
 */
@Slf4j
@Component
public class TestLoginHelper {

    @Autowired(required = false)
    private TestRestTemplate restTemplate;

    /**
     * 使用超级管理员账号登录(userId=1)
     *
     * @return 访问令牌(格式: "Bearer xxx")
     */
    public String loginAsSuperAdmin() {
        return loginAsUser(1L);
    }

    /**
     * 使用指定用户ID登录
     *
     * @param userId 用户ID
     * @return 访问令牌(格式: "Bearer xxx")
     */
    public String loginAsUser(Long userId) {
        log.info("通过/getToken接口获取userId={}的token", userId);

        try {
            // 调用 /getToken 接口
            String url = "/getToken?userId=" + userId;
            String token = restTemplate.getForObject(url, String.class);

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("获取token失败: 返回值为空");
            }

            log.info("获取token成功: {}", token.substring(0, Math.min(30, token.length())) + "...");
            return token;
        } catch (Exception e) {
            log.error("获取token失败: {}", e.getMessage());
            throw new RuntimeException("获取token失败，请确保应用运行在开发环境(dev profile)", e);
        }
    }

}
