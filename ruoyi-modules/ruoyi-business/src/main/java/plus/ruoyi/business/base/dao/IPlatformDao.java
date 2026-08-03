package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Platform;
import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.List;

/**
 * 平台配置DAO接口
 *
 * @author 抓蛙师
 */
public interface IPlatformDao extends IBaseDao<Platform> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Platform> buildQueryWrapper(PlatformBo bo);

    /**
     * 根据appid查询平台配置
     *
     * @param appid appid
     * @return 平台配置
     */
    Platform getByAppid(String appid);

    /**
     * 根据appid和平台类型查询平台配置
     *
     * @param appid  appid
     * @param type   平台类型
     * @param status 状态
     * @return 平台配置
     */
    Platform getByAppidAndType(String appid, String type, String status);

    /**
     * 根据平台类型和状态查询配置列表
     *
     * @param type   平台类型
     * @param status 状态
     * @return 配置列表
     */
    List<Platform> listByTypeAndStatus(String type, String status);
}
