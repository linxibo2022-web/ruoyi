package plus.ruoyi.system.auth.service;


import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;

/**
 * 认证策略接口
 * 定义不同认证方式的统一接口，支持策略模式实现多种登录方式
 *
 * @author Michelle.Chung
 */
public interface IAuthStrategy {

    /**
     * 认证策略的基础名称后缀
     * 用于构建Bean名称，如passwordAuthStrategy、smsAuthStrategy
     */
    String BASE_NAME = "AuthStrategy";

    /**
     * 静态登录方法
     * 根据认证类型选择对应的认证策略实现登录
     *
     * @param body 登录请求体
     * @param authType 认证类型，用于确定使用哪种认证策略
     * @return 登录结果
     */
    static AuthTokenVo login(String body, String authType) {
        // 构建Bean名称 - 认证类型 + AuthStrategy
        String beanName = authType + BASE_NAME;

        // 检查是否存在对应的认证策略实现
        if (!SpringUtils.containsBean(beanName)) {
            throw ServiceException.of("认证方式有误!");
        }

        // 获取认证策略实例
        IAuthStrategy instance = SpringUtils.getBean(beanName);
        // 调用实例的登录方法执行认证
        return instance.login(body);
    }

    /**
     * 登录接口
     * 由各种认证策略实现类实现具体的登录逻辑
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    AuthTokenVo login(String body);

}
