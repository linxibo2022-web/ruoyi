package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IPlatformDao;
import plus.ruoyi.business.base.domain.Platform;
import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.business.base.mapper.PlatformMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.List;
import java.util.Map;

/**
 * 平台配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class PlatformDaoImpl extends BaseDaoImpl<PlatformMapper, Platform> implements IPlatformDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Platform> buildQueryWrapper(PlatformBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Platform> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Platform::getId, bo.getId());
        lqw.eq(Platform::getType, bo.getType());
        lqw.eq(Platform::getName, bo.getName());
        lqw.eq(Platform::getAppid, bo.getAppid());
        lqw.eq(Platform::getSecret, bo.getSecret());
        lqw.eq(Platform::getToken, bo.getToken());
        lqw.eq(Platform::getAeskey, bo.getAeskey());
        lqw.eq(Platform::getPaymentIds, bo.getPaymentIds());
        lqw.eq(Platform::getStatus, bo.getStatus());
        lqw.eq(Platform::getCreateTime, bo.getCreateTime());
        lqw.between(Platform::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Platform::getId, searchValue)              // Long 类型
                .or().like(Platform::getType, searchValue)           // String 类型
                .or().like(Platform::getName, searchValue)           // String 类型
                .or().like(Platform::getAppid, searchValue)          // String 类型
                .or().like(Platform::getSecret, searchValue)         // String 类型
                .or().like(Platform::getToken, searchValue)          // String 类型
                .or().like(Platform::getAeskey, searchValue)         // String 类型
                .or().like(Platform::getPaymentIds, searchValue)     // String 类型
                .or().like(Platform::getTemplateConfigs, searchValue) // String 类型
            );
        }
        return lqw;
    }

    /**
     * 根据appid查询平台配置
     *
     * @param appid appid
     * @return 平台配置
     */
    @Override
    public Platform getByAppid(String appid) {
        PlusLambdaQuery<Platform> lqw = PlusLambdaQuery.of(Platform.class)
            .eq(Platform::getAppid, appid);
        return getOne(lqw);
    }

    /**
     * 根据appid和平台类型查询平台配置
     *
     * @param appid  appid
     * @param type   平台类型
     * @param status 状态
     * @return 平台配置
     */
    @Override
    public Platform getByAppidAndType(String appid, String type, String status) {
        PlusLambdaQuery<Platform> lqw = PlusLambdaQuery.of(Platform.class)
            .eq(Platform::getAppid, appid)
            .eq(Platform::getType, type)
            .eq(Platform::getStatus, status);
        return getOne(lqw);
    }

    /**
     * 根据平台类型和状态查询配置列表
     *
     * @param type   平台类型
     * @param status 状态
     * @return 配置列表
     */
    @Override
    public List<Platform> listByTypeAndStatus(String type, String status) {
        PlusLambdaQuery<Platform> lqw = PlusLambdaQuery.of(Platform.class)
            .eq(Platform::getType, type)
            .eq(Platform::getStatus, status);
        return list(lqw);
    }
}
