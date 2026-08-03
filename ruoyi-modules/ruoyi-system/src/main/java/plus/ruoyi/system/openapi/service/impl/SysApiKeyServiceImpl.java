package plus.ruoyi.system.openapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.vo.OpenApiVo;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.openapi.config.OpenApiProperties;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.openapi.dao.ISysApiKeyDao;
import plus.ruoyi.system.openapi.domain.SysApiKey;
import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;
import plus.ruoyi.system.openapi.domain.vo.OpenApiSecretVo;
import plus.ruoyi.system.openapi.domain.vo.SysApiKeyVo;
import plus.ruoyi.system.openapi.service.ISysApiKeyService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * API密钥Service业务层处理
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@RequiredArgsConstructor
@Service
public class SysApiKeyServiceImpl implements ISysApiKeyService {

    private final ISysApiKeyDao apiKeyDao;
    private final ISysUserService userService;
    private final SysLoginService loginService;
    private final OpenApiProperties openApiProperties;

    private static final String OPEN_API_CACHE_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "openapi:";

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public SysApiKeyVo get(Long id) {
        SysApiKey entity = apiKeyDao.getById(id);
        return MapstructUtils.convert(entity, SysApiKeyVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysApiKeyVo> list(SysApiKeyBo bo) {
        PlusLambdaQuery<SysApiKey> wrapper = apiKeyDao.buildQueryWrapper(bo);
        List<SysApiKey> entities = apiKeyDao.list(wrapper);
        return MapstructUtils.convert(entities, SysApiKeyVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysApiKeyVo> page(SysApiKeyBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysApiKey> wrapper = apiKeyDao.buildQueryWrapper(bo);
        PageResult<SysApiKey> entityPage = apiKeyDao.page(wrapper, pageQuery);
        return entityPage.convert(SysApiKeyVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysApiKeyBo bo) {
        SysApiKey entity = MapstructUtils.convert(bo, SysApiKey.class);
        beforeSave(entity);
        apiKeyDao.insert(entity);
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
    public int update(SysApiKeyBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("API密钥ID不能为空");
        }
        SysApiKey existingKey = apiKeyDao.getById(bo.getId());
        if (existingKey == null) {
            throw ServiceException.of("API密钥不存在");
        }
        SysApiKey entity = MapstructUtils.convert(bo, SysApiKey.class);
        beforeSave(entity);
        int rows = apiKeyDao.updateById(entity);
        // 清除缓存，确保状态、过期时间等变更立即生效
        if (rows > 0 && StringUtils.isNotBlank(existingKey.getAppKey())) {
            RedisUtils.deleteObject(OPEN_API_CACHE_KEY + existingKey.getAppKey());
        }
        return rows;
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
        beforeDelete(ids);
        return apiKeyDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysApiKeyBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysApiKey> entities = new ArrayList<>(boList.size());
        for (SysApiKeyBo bo : boList) {
            SysApiKey entity = MapstructUtils.convert(bo, SysApiKey.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return apiKeyDao.batchSave(entities);
    }

    /**
     * 统计数量
     *
     * @param bo 查询参数
     * @return 统计结果
     */
    @Override
    public Long count(SysApiKeyBo bo) {
        PlusLambdaQuery<SysApiKey> wrapper = apiKeyDao.buildQueryWrapper(bo);
        return apiKeyDao.count(wrapper);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysApiKey entity) {
        // 检查AppKey是否重复
        if (StringUtils.isNotBlank(entity.getAppKey())) {
            boolean unique = TenantHelper.ignore(() -> apiKeyDao.checkAppKeyUnique(entity.getAppKey(), entity.getId()));
            if (!unique) {
                throw ServiceException.of("AppKey已存在");
            }
        }
    }

    /**
     * API密钥数据删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 删除时清除缓存
        List<SysApiKey> apiKeys = apiKeyDao.listByIds(ids);
        apiKeys.forEach(apiKey -> {
            if (apiKey != null && StringUtils.isNotBlank(apiKey.getAppKey())) {
                RedisUtils.deleteObject(OPEN_API_CACHE_KEY + apiKey.getAppKey());
            }
        });
    }

    /**
     * 生成新的API密钥
     *
     * @param bo 业务对象
     * @return 密钥详情(包含明文Secret)
     */
    @Override
    public OpenApiSecretVo generate(SysApiKeyBo bo) {
        // 生成AppKey和AppSecret
        String appKey = IdUtil.simpleUUID();
        String appSecret = IdUtil.simpleUUID();

        // AES加密AppSecret
        String encryptedSecret = EncryptUtils.encryptByAes(appSecret, openApiProperties.getSecretEncryptKey());

        bo.setAppKey(appKey);
        bo.setAppSecret(encryptedSecret);
        bo.setStatus(DictEnableStatus.ENABLE.getValue()); // 默认启用
        bo.setCallCount(0L);

        // 保存到数据库
        Long id = add(bo);

        // 返回明文密钥(仅此一次)
        OpenApiSecretVo secretVo = new OpenApiSecretVo();
        secretVo.setId(id);
        secretVo.setAppName(bo.getAppName());
        secretVo.setAppKey(appKey);
        secretVo.setAppSecret(appSecret);

        return secretVo;
    }

    /**
     * 根据AppKey获取密钥信息(用于认证)
     *
     * @param appKey AppKey
     * @return 密钥信息
     */
    @Override
    public OpenApiVo getByAppKey(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = OPEN_API_CACHE_KEY + appKey;
        OpenApiVo cachedVo = RedisUtils.getCacheObject(cacheKey);
        if (cachedVo != null) {
            // 进行解密密钥
            cachedVo.setAppSecret(EncryptUtils.decryptByAes(cachedVo.getAppSecret(),
                openApiProperties.getSecretEncryptKey()));
            return cachedVo;
        }

        // 从数据库查询
        SysApiKey apiKey = TenantHelper.ignore(() -> apiKeyDao.getByAppKeyAndStatus(appKey, DictEnableStatus.ENABLE.getValue()));

        if (apiKey == null) {
            return null;
        }

        // 转换为 core 模块的 Vo
        OpenApiVo openApiVo = MapstructUtils.convert(apiKey, OpenApiVo.class);

        // 缓存2小时
        RedisUtils.setCacheObject(cacheKey, openApiVo, Duration.ofHours(2));
        // 进行解密密钥
        openApiVo.setAppSecret(EncryptUtils.decryptByAes(openApiVo.getAppSecret(),
            openApiProperties.getSecretEncryptKey()));
        return openApiVo;
    }

    /**
     * 记录API调用
     *
     * @param appKey AppKey
     */
    @Override
    public void recordCall(String appKey) {
        if (StringUtils.isBlank(appKey)) {
            return;
        }

        SysApiKey apiKey = apiKeyDao.getByAppKey(appKey);

        if (apiKey != null) {
            apiKey.setCallCount(apiKey.getCallCount() + 1);
            apiKey.setLastCallTime(new Date());
            apiKeyDao.updateById(apiKey);
        }
    }

    /**
     * 重置密钥(重新生成AppSecret)
     *
     * @param id 密钥ID
     * @return 新的密钥详情
     */
    @Override
    public OpenApiSecretVo resetSecret(Long id) {
        SysApiKey apiKey = apiKeyDao.getById(id);
        if (apiKey == null) {
            throw ServiceException.of("API密钥不存在");
        }

        // 生成新的AppSecret
        String newAppSecret = IdUtil.simpleUUID();
        // AES加密AppSecret
        String encryptedSecret = EncryptUtils.encryptByAes(newAppSecret,
            openApiProperties.getSecretEncryptKey());

        apiKey.setAppSecret(encryptedSecret);
        apiKeyDao.updateById(apiKey);

        // 清除缓存
        RedisUtils.deleteObject(OPEN_API_CACHE_KEY + apiKey.getAppKey());

        // 返回新密钥
        OpenApiSecretVo secretVo = new OpenApiSecretVo();
        secretVo.setId(apiKey.getId());
        secretVo.setAppName(apiKey.getAppName());
        secretVo.setAppKey(apiKey.getAppKey());
        secretVo.setAppSecret(newAppSecret);

        return secretVo;
    }

    @Override
    public LoginUser getLoginUserByUserId(Long userId) {
        // 查询用户基本信息
        SysUserVo user = userService.getUserById(userId);

        if (ObjectUtil.isNull(user)) {
            throw ServiceException.of("用户不存在");
        }

        // 检查用户状态
        if (DictEnableStatus.DISABLED.getValue().equals(user.getStatus())) {
            throw ServiceException.of("用户已被禁用");
        }
        // 设置用户类型为开放API用户
        user.setUserType(UserType.OPENAPI_USER.getUserType());
        // 使用SysLoginService的createLoginUser方法构建完整的LoginUser对象
        // 这样可以确保数据结构与正常登录完全一致
        return loginService.createLoginUser(user);
    }
}
