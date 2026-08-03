package plus.ruoyi.common.core.service;

import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.vo.OpenApiVo;

/**
 * 通用 开放API服务
 *
 * @author 抓蛙师
 */
public interface OpenApiService {

    /**
     * 根据AppKey获取密钥信息(用于认证)
     *
     * @param appKey AppKey
     * @return 密钥信息
     */
    OpenApiVo getByAppKey(String appKey);

    /**
     * 记录API调用
     *
     * @param appKey AppKey
     */
    void recordCall(String appKey);

    /**
     * 根据用户ID获取完整的登录用户信息
     * 包含角色、权限、部门等完整信息
     *
     * @param userId 用户ID
     * @return 登录用户信息
     */
    LoginUser getLoginUserByUserId(Long userId);
}
