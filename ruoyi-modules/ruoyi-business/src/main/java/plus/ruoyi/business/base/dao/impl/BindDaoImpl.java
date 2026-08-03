package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IBindDao;
import plus.ruoyi.business.base.domain.Bind;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.mapper.BindMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Map;

/**
 * 账号绑定数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class BindDaoImpl extends BaseDaoImpl<BindMapper, Bind> implements IBindDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Bind> buildQueryWrapper(BindBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Bind> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Bind::getId, bo.getId());
        lqw.eq(Bind::getUserId, bo.getUserId());
        lqw.eq(Bind::getPlatformType, bo.getPlatformType());
        lqw.eq(Bind::getAppid, bo.getAppid());
        lqw.eq(Bind::getUnionid, bo.getUnionid());
        lqw.eq(Bind::getOpenid, bo.getOpenid());
        lqw.eq(Bind::getExtraData, bo.getExtraData());
        lqw.eq(Bind::getCreateTime, bo.getCreateTime());
        lqw.between(Bind::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Bind::getId, searchValue)              // Long 类型
                .or().likeCast(Bind::getUserId, searchValue)     // Long 类型
                .or().like(Bind::getPlatformType, searchValue)   // String 类型
                .or().like(Bind::getAppid, searchValue)          // String 类型
                .or().like(Bind::getUnionid, searchValue)        // String 类型
                .or().like(Bind::getOpenid, searchValue)         // String 类型
                .or().like(Bind::getExtraData, searchValue)      // String 类型
                .or().likeCast(Bind::getCreateTime, searchValue) // DateTime 类型
            );
        }
        return lqw;
    }

    /**
     * 根据平台类型和openid查询绑定信息
     *
     * @param platformType 平台类型
     * @param openid       openid
     * @return 绑定信息
     */
    @Override
    public Bind getByPlatformAndOpenid(String platformType, String openid) {
        PlusLambdaQuery<Bind> lqw = PlusLambdaQuery.of(Bind.class)
            .eq(Bind::getPlatformType, platformType)
            .eq(Bind::getOpenid, openid)
            .orderByAsc(Bind::getCreateTime);
        // 使用 getOne(lqw, false) 防止多条数据时抛出异常，返回第一条（最早创建的）
        return getOne(lqw, false);
    }
}
