package plus.ruoyi.system.openapi.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.openapi.dao.ISysApiKeyDao;
import plus.ruoyi.system.openapi.domain.SysApiKey;
import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;
import plus.ruoyi.system.openapi.mapper.SysApiKeyMapper;

import java.util.Map;

/**
 * API密钥数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysApiKeyDaoImpl extends BaseDaoImpl<SysApiKeyMapper, SysApiKey> implements ISysApiKeyDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysApiKey> buildQueryWrapper(SysApiKeyBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysApiKey> lqw = PlusLambdaQuery.of(SysApiKey.class);

        // 精确匹配查询条件
        lqw.like(StringUtils.isNotBlank(bo.getAppName()), SysApiKey::getAppName, bo.getAppName());
        lqw.eq(StringUtils.isNotBlank(bo.getAppKey()), SysApiKey::getAppKey, bo.getAppKey());
        lqw.eq(bo.getUserId() != null, SysApiKey::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysApiKey::getStatus, bo.getStatus());
        lqw.between(params.get("beginExpireTime") != null, SysApiKey::getExpireTime,
            params.get("beginExpireTime"), params.get("endExpireTime"));
        lqw.between(params.get("beginCreateTime") != null, SysApiKey::getCreateTime,
            params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysApiKey::getAppName, searchValue)
                .or()
                .like(SysApiKey::getAppKey, searchValue)
                .or()
                .like(SysApiKey::getRemark, searchValue)
            );
        }

        return lqw;
    }

    /**
     * 根据AppKey查询API密钥
     *
     * @param appKey AppKey
     * @return API密钥实体
     */
    @Override
    public SysApiKey getByAppKey(String appKey) {
        PlusLambdaQuery<SysApiKey> lqw = PlusLambdaQuery.of(SysApiKey.class)
            .eq(SysApiKey::getAppKey, appKey);
        return getOne(lqw);
    }

    /**
     * 根据AppKey查询启用状态的API密钥
     *
     * @param appKey AppKey
     * @param status 状态
     * @return API密钥实体
     */
    @Override
    public SysApiKey getByAppKeyAndStatus(String appKey, String status) {
        PlusLambdaQuery<SysApiKey> lqw = PlusLambdaQuery.of(SysApiKey.class)
            .eq(SysApiKey::getAppKey, appKey)
            .eq(SysApiKey::getStatus, status);
        return getOne(lqw);
    }

    /**
     * 校验AppKey是否唯一
     *
     * @param appKey AppKey
     * @param id     排除的ID
     * @return 是否唯一
     */
    @Override
    public boolean checkAppKeyUnique(String appKey, Long id) {
        PlusLambdaQuery<SysApiKey> lqw = PlusLambdaQuery.of(SysApiKey.class)
            .eq(SysApiKey::getAppKey, appKey)
            .ne(SysApiKey::getId, id);
        return !exists(lqw);
    }

}
