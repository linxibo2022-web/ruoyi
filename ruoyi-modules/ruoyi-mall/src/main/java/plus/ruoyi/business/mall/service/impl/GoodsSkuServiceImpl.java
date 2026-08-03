package plus.ruoyi.business.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.mall.dao.IGoodsSkuDao;
import plus.ruoyi.business.mall.domain.GoodsSku;
import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.business.mall.domain.vo.GoodsSkuVo;
import plus.ruoyi.business.mall.service.IGoodsSkuService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 商品SKU服务实现
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Service
@RequiredArgsConstructor
public class GoodsSkuServiceImpl implements IGoodsSkuService {

    private final IGoodsSkuDao goodsSkuDao;

    /**
     * 根据ID查询
     */
    @Override
    public GoodsSkuVo get(Long id) {
        GoodsSku entity = goodsSkuDao.getById(id);
        return MapstructUtils.convert(entity, GoodsSkuVo.class);
    }

    /**
     * 根据商品ID查询SKU列表
     */
    @Override
    public List<GoodsSkuVo> listByGoodsId(Long goodsId) {
        PlusLambdaQuery<GoodsSku> wrapper = PlusLambdaQuery.of();
        wrapper.eq(GoodsSku::getGoodsId, goodsId);
        wrapper.orderByAsc(GoodsSku::getSortOrder);
        List<GoodsSku> entities = goodsSkuDao.list(wrapper);
        return MapstructUtils.convert(entities, GoodsSkuVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<GoodsSkuVo> list(GoodsSkuBo bo) {
        PlusLambdaQuery<GoodsSku> wrapper = goodsSkuDao.buildQueryWrapper(bo);
        List<GoodsSku> entities = goodsSkuDao.list(wrapper);
        return MapstructUtils.convert(entities, GoodsSkuVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<GoodsSkuVo> page(GoodsSkuBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<GoodsSku> wrapper = goodsSkuDao.buildQueryWrapper(bo);
        PageResult<GoodsSku> entityPage = goodsSkuDao.page(wrapper, pageQuery);
        return entityPage.convert(GoodsSkuVo.class);
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(GoodsSkuBo bo) {
        // 1. 验证规格值格式和维度一致性
        validateSpecValues(bo.getGoodsId(), bo.getSpecValues());

        // 2. 检查规格组合唯一性
        checkSpecUnique(bo.getGoodsId(), bo.getSpecValues(), null);

        // 3. 校验默认SKU唯一性
        validateDefaultSku(bo.getGoodsId(), bo.getIsDefault(), null);

        GoodsSku entity = MapstructUtils.convert(bo, GoodsSku.class);
        generateSkuCode(entity);
        generateSkuName(entity); // 自动生成SKU名称
        goodsSkuDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(GoodsSkuBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("SKU ID不能为空");
        }
        if (!goodsSkuDao.exists(bo.getId())) {
            throw ServiceException.of("SKU不存在");
        }

        // 1. 验证规格值格式和维度一致性
        validateSpecValues(bo.getGoodsId(), bo.getSpecValues());

        // 2. 检查规格组合唯一性(排除自身)
        checkSpecUnique(bo.getGoodsId(), bo.getSpecValues(), bo.getId());

        // 3. 校验默认SKU唯一性(排除自身)
        validateDefaultSku(bo.getGoodsId(), bo.getIsDefault(), bo.getId());

        GoodsSku entity = MapstructUtils.convert(bo, GoodsSku.class);
        generateSkuName(entity); // 自动生成SKU名称
        return goodsSkuDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        return goodsSkuDao.deleteByIds(ids);
    }

    /**
     * 批量保存(智能保存：有ID则更新，无ID则新增，数据库有但前端没传的则删除)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveByGoodsId(Long goodsId, List<GoodsSkuBo> boList) {
        if (goodsId == null) {
            throw ServiceException.of("商品ID不能为空");
        }

        // 1. 查询数据库中该商品现有的所有SKU ID
        PlusLambdaQuery<GoodsSku> query = PlusLambdaQuery.of();
        query.eq(GoodsSku::getGoodsId, goodsId);
        query.select(GoodsSku::getId);
        List<Long> existingSkuIds = goodsSkuDao.mapList(query, obj -> (Long) obj);

        // 2. 收集前端传来的SKU ID（过滤掉null）
        List<Long> submittedSkuIds = new ArrayList<>();
        if (CollUtil.isNotEmpty(boList)) {
            for (GoodsSkuBo bo : boList) {
                if (bo.getId() != null) {
                    submittedSkuIds.add(bo.getId());
                }
            }
        }

        // 3. 找出需要删除的SKU（数据库有，但前端没传）
        List<Long> idsToDelete = new ArrayList<>();
        for (Long existingId : existingSkuIds) {
            if (!submittedSkuIds.contains(existingId)) {
                idsToDelete.add(existingId);
            }
        }

        // 4. 删除被移除的SKU
        if (CollUtil.isNotEmpty(idsToDelete)) {
            goodsSkuDao.deleteByIds(idsToDelete);
        }

        // 5. 批量保存/更新SKU（有ID则更新，无ID则新增）
        if (CollUtil.isEmpty(boList)) {
            return true;
        }

        List<GoodsSku> entities = new ArrayList<>(boList.size());
        for (GoodsSkuBo bo : boList) {
            bo.setGoodsId(goodsId); // 确保goodsId正确

            // 1. 验证规格值格式和维度一致性
            validateSpecValues(bo.getGoodsId(), bo.getSpecValues());

            // 2. 检查规格组合唯一性
            checkSpecUnique(bo.getGoodsId(), bo.getSpecValues(), bo.getId());

            // 3. 校验默认SKU唯一性
            validateDefaultSku(bo.getGoodsId(), bo.getIsDefault(), bo.getId());

            GoodsSku entity = MapstructUtils.convert(bo, GoodsSku.class);
            // 只有新增的SKU才生成编码
            if (entity.getId() == null) {
                generateSkuCode(entity);
            }
            generateSkuName(entity); // 自动生成SKU名称
            entities.add(entity);
        }
        return goodsSkuDao.batchSave(entities) > 0;
    }

    /**
     * 根据商品ID删除所有SKU
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByGoodsId(Long goodsId) {
        if (goodsId == null) {
            return false;
        }
        PlusLambdaQuery<GoodsSku> wrapper = PlusLambdaQuery.of();
        wrapper.eq(GoodsSku::getGoodsId, goodsId);
        return goodsSkuDao.delete(wrapper) > 0;
    }

    /**
     * 生成SKU编码
     * 规则: 商品ID + "-" + 规格值哈希(前6位) + "-" + 序号(3位)
     * 示例: 1001-A3F2D1-001
     *
     * 优势:
     * 1. 包含商品ID,便于溯源
     * 2. 包含规格值哈希,避免随机性,同规格多次生成编码一致
     * 3. 短小精悍,易于管理
     */
    private void generateSkuCode(GoodsSku entity) {
        if (StringUtils.isBlank(entity.getSkuCode())) {
            // 1. 获取规格值哈希(前6位大写)
            String specHash = "";
            if (StringUtils.isNotBlank(entity.getSpecValues())) {
                // 使用MD5哈希规格值JSON
                specHash = cn.hutool.crypto.digest.DigestUtil
                    .md5Hex(entity.getSpecValues())
                    .substring(0, 6)
                    .toUpperCase();
            }

            // 2. 生成序号(3位随机数)
            String sequence = IdUtil.fastSimpleUUID().substring(0, 3).toUpperCase();

            // 3. 组合编码
            entity.setSkuCode(entity.getGoodsId() + "-" + specHash + "-" + sequence);
        }
    }

    /**
     * 验证规格值JSON格式和维度一致性
     * 核心规则:
     * 1. specValues必须是合法的JSON对象
     * 2. JSON的key必须按字母排序(统一格式,便于后续比对)
     * 3. 同一商品下所有SKU的规格维度(key集合)必须完全一致
     * 4. 规格维度不能为空
     *
     * @param goodsId 商品ID
     * @param specValues 规格值JSON字符串
     */
    private void validateSpecValues(Long goodsId, String specValues) {
        // 1. 校验JSON格式
        if (StringUtils.isBlank(specValues)) {
            throw ServiceException.of("规格值不能为空");
        }

        JSONObject specJson;
        try {
            specJson = JSONUtil.parseObj(specValues);
        } catch (Exception e) {
            throw ServiceException.of("规格值格式错误,必须是有效的JSON对象,如:{\"颜色\":\"红色\",\"尺码\":\"S\"}");
        }

        if (specJson.isEmpty()) {
            throw ServiceException.of("规格值不能为空对象");
        }

        // 2. 检查规格值是否都有内容
        for (Map.Entry<String, Object> entry : specJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (StringUtils.isBlank(key)) {
                throw ServiceException.of("规格维度名称不能为空");
            }
            if (value == null || StringUtils.isBlank(value.toString())) {
                throw ServiceException.of("规格维度[" + key + "]的值不能为空");
            }
        }

        // 3. 查询同商品下已有的SKU,检查规格维度一致性
        List<GoodsSkuVo> existingSkus = listByGoodsId(goodsId);
        if (CollUtil.isNotEmpty(existingSkus)) {
            // 获取第一个SKU的规格维度作为标准
            String firstSpecValues = existingSkus.get(0).getSpecValues();
            JSONObject firstSpecJson = JSONUtil.parseObj(firstSpecValues);

            // 提取规格维度(key集合),使用TreeSet自动排序
            Set<String> standardKeys = new TreeSet<>(firstSpecJson.keySet());
            Set<String> currentKeys = new TreeSet<>(specJson.keySet());

            // 校验规格维度是否一致
            if (!standardKeys.equals(currentKeys)) {
                throw ServiceException.of("规格维度必须与已有SKU一致。" +
                    "标准维度:" + standardKeys + ",当前维度:" + currentKeys);
            }
        }
    }

    /**
     * 检查规格组合唯一性
     * 防止创建完全相同规格的SKU(如:两个"红色-S码")
     *
     * @param goodsId 商品ID
     * @param specValues 规格值JSON字符串
     * @param excludeId 排除的SKU ID(修改时使用,排除自身)
     */
    private void checkSpecUnique(Long goodsId, String specValues, Long excludeId) {
        // 1. 标准化规格值JSON(按key排序后重新生成JSON,确保格式统一)
        JSONObject specJson = JSONUtil.parseObj(specValues);
        Map<String, Object> sortedSpec = new java.util.TreeMap<>(specJson);
        String normalizedSpecValues = JSONUtil.toJsonStr(sortedSpec);

        // 2. 查询同商品下所有SKU
        List<GoodsSkuVo> existingSkus = listByGoodsId(goodsId);

        // 3. 检查是否存在相同的规格组合
        for (GoodsSkuVo sku : existingSkus) {
            // 排除自身(修改时)
            if (excludeId != null && excludeId.equals(sku.getId())) {
                continue;
            }

            // 标准化已有SKU的规格值
            JSONObject existingSpecJson = JSONUtil.parseObj(sku.getSpecValues());
            Map<String, Object> existingSortedSpec = new java.util.TreeMap<>(existingSpecJson);
            String existingNormalizedSpec = JSONUtil.toJsonStr(existingSortedSpec);

            // 比对规格组合
            if (normalizedSpecValues.equals(existingNormalizedSpec)) {
                // 提取规格值用于友好提示
                String specDisplay = sortedSpec.values().toString()
                    .replaceAll("\\[|\\]", "");
                throw ServiceException.of("该规格组合已存在: " + specDisplay +
                    " (SKU ID: " + sku.getId() + ")");
            }
        }
    }

    /**
     * 自动生成SKU名称
     * 规则: 从specValues中提取所有值,按key排序后用"-"连接
     * 示例: {"颜色":"红色","尺码":"S"} -> "红色-S"
     *
     * @param entity SKU实体
     */
    private void generateSkuName(GoodsSku entity) {
        if (StringUtils.isBlank(entity.getSpecValues())) {
            return;
        }

        try {
            JSONObject specJson = JSONUtil.parseObj(entity.getSpecValues());
            // 使用TreeMap自动按key排序
            Map<String, Object> sortedSpec = new java.util.TreeMap<>(specJson);

            // 提取所有值并用"-"连接
            List<String> values = new ArrayList<>();
            for (Object value : sortedSpec.values()) {
                if (value != null && StringUtils.isNotBlank(value.toString())) {
                    values.add(value.toString());
                }
            }

            if (CollUtil.isNotEmpty(values)) {
                entity.setSkuName(String.join("-", values));
            }
        } catch (Exception e) {
            // JSON解析失败时不设置名称
        }
    }

    /**
     * 校验默认SKU唯一性
     * 规则: 同一商品下只能有一个默认SKU(is_default='1')
     *
     * @param goodsId 商品ID
     * @param isDefault 是否默认(1是 0否)
     * @param excludeId 排除的SKU ID(修改时使用,排除自身)
     */
    private void validateDefaultSku(Long goodsId, String isDefault, Long excludeId) {
        // 只有设置为默认时才需要校验
        if (!"1".equals(isDefault)) {
            return;
        }

        // 查询该商品下是否已有默认SKU
        PlusLambdaQuery<GoodsSku> query = PlusLambdaQuery.of();
        query.eq(GoodsSku::getGoodsId, goodsId);
        query.eq(GoodsSku::getIsDefault, "1");

        // 修改时排除自身
        if (excludeId != null) {
            query.ne(GoodsSku::getId, excludeId);
        }

        long count = goodsSkuDao.count(query);
        if (count > 0) {
            throw ServiceException.of("该商品已存在默认SKU,一个商品只能有一个默认SKU");
        }
    }
}
