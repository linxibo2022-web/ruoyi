package plus.ruoyi.common.core.service;

import plus.ruoyi.common.core.domain.dto.PlatformDTO;

import java.util.List;
import java.util.Collections;

/**
 * 通用 平台配置服务
 *
 * @author 抓蛙师
 */
public interface PlatformService {

    /**
     * 根据平台类型获取平台配置列表
     *
     * @param type     平台类型
     * @param tenantId 租户id
     * @return 平台配置列表
     */
    default List<PlatformDTO> listPlatformsByType(String type, String tenantId) {
        return Collections.emptyList();
    }

    /**
     * 根据appid和平台类型获取平台配置
     *
     * @param appid appid
     * @param type  平台类型
     * @return 平台配置
     */
    default PlatformDTO getPlatformByAppidAndType(String appid, String type) {
        return null;
    }

    /**
     * 根据appid获取平台配置（跨租户查询）
     * 用于检查appid的全局唯一性
     *
     * @param appid    appid
     * @param tenantId 租户id
     * @return 平台配置，如果不存在返回null
     */
    default PlatformDTO getPlatformByAppid(String appid, String tenantId) {
        return null;
    }

}
