---
name: crud-development
description: |
  开发 CRUD 功能、创建业务模块。包含后端四层架构 + 前端 API/Types 数据通道（页面 UI 见 ui-pc / ui-mobile）。

  触发场景：
  - 创建新的业务模块（如"用户反馈"、"优惠券"）
  - 编写后端代码：Entity、BO、VO、Service、DAO、Controller、Mapper
  - 编写前端 API 和 TypeScript 类型定义
  - 任何涉及"增删改查"的全栈开发

  触发词：CRUD、增删改查、新建模块、Entity、Service、DAO、Controller、BO、VO、Mapper、业务模块、后端代码、Java代码、xxxApi.ts、xxxTypes.ts

  核心警告：本项目不是 ruoyi-vue-plus！
  - 包名是 plus.ruoyi（不是 com.ruoyi）
  - Service 不继承基类
  - 有独立 DAO 层，查询条件在 DAO.buildQueryWrapper() 构建

  注意：如果只是写前端页面 UI，请使用 ui-pc 或 ui-mobile。
---

# CRUD 全栈开发规范

> **严重警告**: 本项目是 **ruoyi-plus-uniapp**，不是 ruoyi-vue-plus！
> 架构设计完全不同，必须严格遵循本规范！

## 核心架构区别

| 对比项 | ruoyi-vue-plus | 本项目 (ruoyi-plus-uniapp) |
|--------|---------------|---------------------------|
| **包名前缀** | `com.ruoyi` | `plus.ruoyi` |
| **Service继承** | 继承 `ServiceImpl` | **不继承任何基类** |
| **DAO层** | 无独立DAO层 | **有独立DAO层** |
| **Mapper继承** | 继承 `BaseMapperPlus` | 只继承 `BaseMapper<Entity>` |
| **查询构建** | Service层直接构建 | **DAO层 buildQueryWrapper()** |
| **对象转换** | BeanUtil | **MapstructUtils** |
| **实体基类** | BaseEntity | **TenantEntity** |
| **BO注解** | @TableField | **@AutoMappers** |

---

## 0. 命名规则（强制执行）⭐⭐⭐⭐⭐

### businessName 计算规则

**公式**：表名去掉第一个 `_` 及其之前的部分，然后转驼峰命名。

| 表名 | 去前缀后 | businessName | ClassName |
|------|---------|-------------|-----------|
| `b_ad` | `ad` | `ad` | `Ad` |
| `b_feedback` | `feedback` | `feedback` | `Feedback` |
| `b_goods_category` | `goods_category` | `goodsCategory` | `GoodsCategory` |
| `m_order_item` | `order_item` | `orderItem` | `OrderItem` |
| `sys_user` | `user` | `user` | `User` |

### 后端文件路径规则

所有后端文件在 `{packageName}/{businessName 中不含子路径}` 包下，文件名基于 `ClassName`：

```
{javaPath}/domain/{ClassName}.java
{javaPath}/domain/bo/{ClassName}Bo.java
{javaPath}/domain/vo/{ClassName}Vo.java
{javaPath}/mapper/{ClassName}Mapper.java
{javaPath}/dao/I{ClassName}Dao.java
{javaPath}/dao/impl/{ClassName}DaoImpl.java
{javaPath}/service/I{ClassName}Service.java
{javaPath}/service/impl/{ClassName}ServiceImpl.java
{javaPath}/controller/{ClassName}Controller.java
```

> **注意**：后端 Java 包路径中**不含** businessName 子包！如 `plus.ruoyi.business.base.controller.AdController`（不是 `plus.ruoyi.business.base.ad.controller.AdController`）。

### 前端文件路径规则

前端文件使用 `businessName`（小驼峰）作为文件夹名和文件名：

```
plus-ui/src/api/{frontendPath}/{businessName}/{businessName}Api.ts
plus-ui/src/api/{frontendPath}/{businessName}/{businessName}Types.ts
plus-ui/src/views/{frontendPath}/{businessName}/{businessName}.vue
```

**示例**：表 `b_goods_category`，packageName = `plus.ruoyi.business.base`
- frontendPath = `business/base`
- businessName = `goodsCategory`
- 前端 API: `plus-ui/src/api/business/base/goodsCategory/goodsCategoryApi.ts`
- 前端页面: `plus-ui/src/views/business/base/goodsCategory/goodsCategory.vue`

### 复数后缀规则（pluralSuffix）

用于 API 路径中的复数形式：
- businessName 以 `s` 结尾 → 不加后缀（如 `goods` → `pageGoods`）
- businessName 不以 `s` 结尾 → 加 `s`（如 `ad` → `pageAds`）

---

## 1. Entity 实体类

```java
package plus.ruoyi.business.base.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * XXX对象 b_xxx
 *
 * @author 抓蛙师
 * @date {当前日期}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_xxx")
public class Xxx extends TenantEntity {  // ✅ 继承 TenantEntity

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 名称
     */
    private String xxxName;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;
}
```

---

## 2. BO 业务对象

```java
package plus.ruoyi.business.base.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import io.github.linpeilie.annotations.AutoMapper;
import plus.ruoyi.business.base.domain.Xxx;
import plus.ruoyi.business.base.domain.vo.XxxVo;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * XXX业务对象 b_xxx
 *
 * @author 抓蛙师
 * @date {当前日期}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({  // ✅ 映射到 Entity 和 VO
    @AutoMapper(target = Xxx.class, reverseConvertGenerate = false),
    @AutoMapper(target = XxxVo.class)
})
public class XxxBo extends BaseEntity {

    /**
     * 主键id
     */
    @NotNull(message = "主键id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String xxxName;

    /**
     * 状态
     */
    private String status;
}
```

---

## 3. VO 视图对象

```java
package plus.ruoyi.business.base.domain.vo;

import plus.ruoyi.business.base.domain.Xxx;
import plus.ruoyi.business.base.domain.bo.XxxBo;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMappers;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * XXX视图对象 b_xxx
 *
 * @author 抓蛙师
 * @date {当前日期}
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({  // ✅ VO 也使用 @AutoMappers，映射到 Entity 和 Bo
    @AutoMapper(target = Xxx.class),
    @AutoMapper(target = XxxBo.class)
})
public class XxxVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @ExcelProperty(value = "主键id")
    private Long id;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String xxxName;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_enable_status")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;
}
```

---

## 4. Service 接口

```java
package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.XxxBo;
import plus.ruoyi.business.base.domain.vo.XxxVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import java.util.Collection;
import java.util.List;

/**
 * XXX服务接口
 */
public interface IXxxService {  // ✅ 不继承 IBaseService

    /**
     * 根据ID查询
     */
    XxxVo get(Long id);

    /**
     * 查询列表
     */
    List<XxxVo> list(XxxBo bo);

    /**
     * 分页查询
     */
    PageResult<XxxVo> page(XxxBo bo, PageQuery pageQuery);

    /**
     * 新增
     */
    Long add(XxxBo bo);

    /**
     * 修改
     */
    int update(XxxBo bo);

    /**
     * 批量删除
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     */
    int batchSave(List<XxxBo> boList);

    /**
     * 查询选项列表
     * 用于下拉选择、关联查询等场景
     */
    List<XxxVo> listForOption();
}
```

---

## 5. Service 实现类 (核心！不继承任何基类)

```java
package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IXxxDao;
import plus.ruoyi.business.base.domain.Xxx;
import plus.ruoyi.business.base.domain.bo.XxxBo;
import plus.ruoyi.business.base.domain.vo.XxxVo;
import plus.ruoyi.business.base.service.IXxxService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * XXX服务实现
 *
 * @author 抓蛙师
 * @date {当前日期}
 */
@Service
@RequiredArgsConstructor
public class XxxServiceImpl implements IXxxService {  // ✅ 不继承任何基类！

    private final IXxxDao xxxDao;  // ✅ 只注入 DAO，不注入 Mapper

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public XxxVo get(Long id) {
        Xxx entity = xxxDao.getById(id);
        return MapstructUtils.convert(entity, XxxVo.class);  // ✅ 使用 MapstructUtils
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<XxxVo> list(XxxBo bo) {
        PlusLambdaQuery<Xxx> wrapper = xxxDao.buildQueryWrapper(bo);  // ✅ DAO 层构建查询
        List<Xxx> entities = xxxDao.list(wrapper);
        return MapstructUtils.convert(entities, XxxVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<XxxVo> page(XxxBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Xxx> wrapper = xxxDao.buildQueryWrapper(bo);
        PageResult<Xxx> entityPage = xxxDao.page(wrapper, pageQuery);
        return entityPage.convert(XxxVo.class);  // ✅ PageResult 自带 convert
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(XxxBo bo) {
        Xxx entity = MapstructUtils.convert(bo, Xxx.class);
        beforeSave(entity);
        xxxDao.insert(entity);
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
    public int update(XxxBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("ID不能为空");
        }
        if (!xxxDao.exists(bo.getId())) {  // ✅ 使用 DAO 的 exists 方法
            throw ServiceException.of("数据不存在");
        }
        Xxx entity = MapstructUtils.convert(bo, Xxx.class);
        beforeSave(entity);
        return xxxDao.updateById(entity);
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
        return xxxDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<XxxBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Xxx> entities = new ArrayList<>(boList.size());
        for (XxxBo bo : boList) {
            Xxx entity = MapstructUtils.convert(bo, Xxx.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return xxxDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Xxx entity) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 删除前钩子方法
     * 子类可重写此方法实现关联数据校验、清理等逻辑
     *
     * @param ids 待删除的ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 查询选项列表
     * 用于下拉选择、关联查询等场景,只返回必要字段
     *
     * @return 选项列表
     */
    @Override
    public List<XxxVo> listForOption() {
        PlusLambdaQuery<Xxx> wrapper = PlusLambdaQuery.of();
        // 只查询启用状态的数据
        wrapper.eq(Xxx::getStatus, "1");
        // 只选择必要的字段:ID和显示字段
        wrapper.select(Xxx::getId, Xxx::getXxxName);
        // 按排序字段排序（如有）
        // wrapper.orderByAsc(Xxx::getSortOrder);
        List<Xxx> entities = xxxDao.list(wrapper);
        return MapstructUtils.convert(entities, XxxVo.class);
    }
}
```

---

## 6. DAO 接口 (本项目独有！)

```java
package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Xxx;
import plus.ruoyi.business.base.domain.bo.XxxBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * XXX DAO接口
 */
public interface IXxxDao extends IBaseDao<Xxx> {

    /**
     * 根据业务对象构建查询条件
     * ✅ 这是本项目的核心设计！
     */
    PlusLambdaQuery<Xxx> buildQueryWrapper(XxxBo bo);
}
```

---

## 7. DAO 实现类 (核心！buildQueryWrapper)

```java
package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IXxxDao;
import plus.ruoyi.business.base.domain.Xxx;
import plus.ruoyi.business.base.domain.bo.XxxBo;
import plus.ruoyi.business.base.mapper.XxxMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import java.util.Map;

/**
 * XXX数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class XxxDaoImpl extends BaseDaoImpl<XxxMapper, Xxx> implements IXxxDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Xxx> buildQueryWrapper(XxxBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Xxx> lqw = PlusLambdaQuery.of();

        // ✅ 查询条件（名称类用 like，其他用 eq，自动处理 null）
        lqw.eq(Xxx::getId, bo.getId());
        lqw.like(Xxx::getXxxName, bo.getXxxName());
        lqw.eq(Xxx::getStatus, bo.getStatus());

        // ✅ 时间范围查询
        lqw.between(Xxx::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // ✅ 模糊搜索多字段（跨数据库兼容）
        // String 类型用 like()，非 String 类型用 likeCast()
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Xxx::getId, searchValue)          // Long 类型 → likeCast
                .or().like(Xxx::getXxxName, searchValue)    // String 类型 → like
                .or().like(Xxx::getRemark, searchValue)     // String 类型 → like
                .or().likeCast(Xxx::getSortOrder, searchValue)   // Integer 类型 → likeCast
                .or().likeCast(Xxx::getCreateTime, searchValue)  // Date 类型 → likeCast
            );
        }

        return lqw;
    }
}
```

---

## 8. Mapper 接口

```java
package plus.ruoyi.business.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import plus.ruoyi.business.base.domain.Xxx;

/**
 * XXX Mapper接口
 * ✅ 只继承 BaseMapper，不继承 BaseMapperPlus
 */
public interface XxxMapper extends BaseMapper<Xxx> {
    // 无需额外方法，BaseDaoImpl 已提供完整 CRUD
}
```

---

## 9. Controller 控制器

> ⚠️ **重要规范**：接口路径和方法名必须包含实体名，确保全局唯一！

### 接口路径规范

| 操作 | HTTP方法 | 路径格式 | 方法名格式 |
|------|---------|---------|-----------|
| 分页查询 | GET | `/page{实体复数}` | `page{实体复数}()` |
| 列表查询 | GET | `/list{实体复数}` | `list{实体复数}()` |
| 获取详情 | GET | `/get{实体}/{id}` | `get{实体}()` |
| 新增 | POST | `/add{实体}` | `add{实体}()` |
| 修改 | PUT | `/update{实体}` | `update{实体}()` |
| 删除 | DELETE | `/delete{实体复数}/{ids}` | `delete{实体复数}()` |
| 导出 | POST | `/export{实体复数}` | `export{实体复数}()` |
| 选项列表 | GET | `/option{实体复数}` | `option{实体复数}()` |

```java
// ❌ 错误：通用路径，不唯一
@GetMapping("/page")
@GetMapping("/{id}")
@PostMapping("/add")

// ✅ 正确：路径包含实体名
@GetMapping("/pageAds")
@GetMapping("/getAd/{id}")
@PostMapping("/addAd")
```

### Controller 模板

```java
package plus.ruoyi.business.base.controller;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.core.domain.vo.DictItemVo;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.business.base.domain.vo.XxxVo;
import plus.ruoyi.business.base.domain.bo.XxxBo;
import plus.ruoyi.business.base.service.IXxxService;

/**
 * XXX管理
 *
 * @author 抓蛙师
 * @date {当前日期}
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/xxx")
public class XxxController {

    private final IXxxService xxxService;  // ✅ 只注入 Service

    /**
     * 查询XXX列表
     */
    @SaCheckPermission("base:xxx:query")
    @GetMapping("/pageXxxs")
    public R<PageResult<XxxVo>> pageXxxs(XxxBo bo, PageQuery pageQuery) {
        return R.ok(xxxService.page(bo, pageQuery));
    }

    /**
     * 获取XXX详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:xxx:query")
    @GetMapping("/getXxx/{id}")
    public R<XxxVo> getXxx(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(xxxService.get(id));
    }

    /**
     * 新增XXX
     */
    @SaCheckPermission("base:xxx:add")
    @Log(title = "XXX管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addXxx")
    public R<Long> addXxx(@Validated(AddGroup.class) @RequestBody XxxBo bo) {
        return R.ok(xxxService.add(bo));
    }

    /**
     * 修改XXX
     */
    @SaCheckPermission("base:xxx:update")
    @Log(title = "XXX管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateXxx")
    public R<Void> updateXxx(@Validated(EditGroup.class) @RequestBody XxxBo bo) {
        return R.status(xxxService.update(bo));
    }

    /**
     * 删除XXX
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:xxx:delete")
    @Log(title = "XXX管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteXxxs/{ids}")
    public R<Void> deleteXxxs(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(xxxService.batchDelete(List.of(ids)));
    }

    /**
     * 导出XXX列表
     */
    @SaCheckPermission("base:xxx:export")
    @Log(title = "XXX管理", operType = DictOperType.EXPORT)
    @PostMapping("/exportXxxs")
    public void exportXxxs(XxxBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<XxxVo> pageResult = xxxService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "XXX数据", XxxVo.class, response);
    }

    /**
     * 获取XXX导入模板
     */
    @PostMapping("/templateXxxs")
    public void templateXxxs(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "XXX数据模板", XxxVo.class, response);
    }

    /**
     * 导入XXX
     *
     * @param file 导入文件
     */
    @Log(title = "XXX管理", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:xxx:import")
    @PostMapping(value = "/importXxxs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importXxxs(MultipartFile file) throws Exception {
        ExcelResult<XxxVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), XxxVo.class, true);
        List<XxxBo> boList = MapstructUtils.convert(excelResult.getList(), XxxBo.class);
        xxxService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 获取XXX选项列表
     * 用于下拉选择、关联查询等场景
     */
    @SaCheckPermission("base:xxx:query")
    @GetMapping("/optionXxxs")
    public R<List<DictItemVo>> optionXxxs() {
        List<XxxVo> list = xxxService.listForOption();
        List<DictItemVo> options = StreamUtils.toList(list, item ->
            DictItemVo.of(item.getXxxName(), String.valueOf(item.getId()), "success")
        );
        return R.ok(options);
    }
}
```

---

## 10. 前端 API 定义

> **注意**: `http`、`Result`、`PageResult` 已全局自动导入，无需手动 import！

```typescript
// plus-ui/src/api/business/base/xxx/xxxApi.ts
import type { XxxQuery, XxxBo, XxxVo } from './xxxTypes'  // ✅ 只需导入自定义类型

/**
 * 查询列表
 */
export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get<PageResult<XxxVo>>('/base/xxx/pageXxxs', query)  // ✅ http 已自动导入
}

/**
 * 查询详细
 */
export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get<XxxVo>(`/base/xxx/getXxx/${id}`)
}

/**
 * 新增
 */
export const addXxx = (data: XxxBo): Result<string | number> => {
  return http.post<string | number>('/base/xxx/addXxx', data)
}

/**
 * 修改
 */
export const updateXxx = (data: XxxBo): Result<void> => {
  return http.put<void>('/base/xxx/updateXxx', data)
}

/**
 * 删除
 */
export const deleteXxxs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/xxx/deleteXxxs/${ids}`)
}

/**
 * 获取选项列表
 * 用于下拉选择、关联查询等场景
 */
export const optionXxxs = (): Result<DictItem[]> => {
  return http.get<DictItem[]>('/base/xxx/optionXxxs')
}
```

---

## 11. 前端类型定义

> **注意**: `PageQuery` 是全局类型，直接 extends 即可！

```typescript
// plus-ui/src/api/business/base/xxx/xxxTypes.ts

/** 查询类型 */
export interface XxxQuery extends PageQuery {  // ✅ PageQuery 全局可用
  id?: string | number
  xxxName?: string
  status?: string
}

/** 表单类型 */
export interface XxxBo {
  id?: string | number
  xxxName?: string
  status?: string
  remark?: string
}

/** 视图类型 */
export interface XxxVo {
  id: string | number
  xxxName: string
  status: string
  createTime: string
  remark: string
}
```

---

## 前端自动导入说明

以下内容已全局自动导入，**无需手动 import**：

### 全局类型（types/global.d.ts）
- `Result<T>` - API 返回类型
- `PageResult<T>` - 分页结果类型
- `PageQuery` - 分页查询参数

### 自动导入（types/auto-imports.d.ts）
- `http` - HTTP 请求工具
- `ref`, `reactive`, `computed`, `watch` 等 Vue APIs
- `useRouter`, `useRoute` - Vue Router
- `ElMessage`, `ElMessageBox` 等 Element Plus
- `storeToRefs` - Pinia
- Store 函数：`useUserStore`, `useDictStore`, `useFeatureStore` 等

### 需要手动导入的
- 自定义类型文件：`XxxQuery`, `XxxBo`, `XxxVo`
- 业务 API 函数

---

## 参考模块（Ad 广告模块）

> ⚠️ **强制要求**：开发前必须先阅读广告模块的真实代码，**对照模仿** Entity/BO/VO/Service/DAO/Controller 以及前端 API/Types 的每一处写法、命名、注释格式。

### 后端参考

```
ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/
├── controller/AdController.java       # Controller 标准写法、接口路径命名
├── service/IAdService.java            # Service 接口（不继承基类）
├── service/impl/AdServiceImpl.java    # Service 实现（注入 IAdDao）
├── dao/IAdDao.java                    # DAO 接口（含 buildQueryWrapper）
├── dao/impl/AdDaoImpl.java            # DAO 实现（继承 BaseDaoImpl）
├── mapper/AdMapper.java               # Mapper（只继承 BaseMapper）
└── domain/
    ├── Ad.java                        # Entity（继承 TenantEntity）
    ├── bo/AdBo.java                   # BO（@AutoMappers 映射）
    └── vo/AdVo.java                   # VO（@AutoMappers + Excel 导出注解）
```

### 前端参考（本技能产出 API/Types 时必读）

```
plus-ui/src/api/business/base/ad/
├── adApi.ts        # API 调用模板（http.get/post/put/del 标准用法）
└── adTypes.ts      # 类型定义模板（XxxQuery extends PageQuery、XxxBo、XxxVo）
```

### 前端页面参考（本技能**不**生成页面 UI，但需对齐风格）

```
plus-ui/src/views/business/base/ad/ad.vue
```

> 完整的列表页面（搜索表单 / 表格 / 弹窗表单 / 分页）→ 激活 `ui-pc` 技能后**对照 `ad.vue` 模仿**。

---

## 检查清单

生成代码前必须检查：

- [ ] 包名是否是 `plus.ruoyi.*`？
- [ ] Service 是否不继承任何基类？
- [ ] DAO 是否有 `buildQueryWrapper()` 和 `exists()` 方法？
- [ ] Entity 是否继承 `TenantEntity`？
- [ ] BO 是否使用 `@AutoMappers` 映射到 Entity 和 VO？
- [ ] VO 是否使用 `@AutoMappers` 映射到 Entity 和 Bo？
- [ ] 是否使用 `MapstructUtils.convert()` 转换对象？
- [ ] 是否所有类型都先 import 再使用简单类名？
- [ ] **接口路径是否包含实体名**（如 `/pageAds` 而非 `/page`）？
- [ ] **Controller 方法名是否包含实体名**（如 `pageAds()` 而非 `page()`）？
- [ ] 前端是否只导入了自定义类型（http/Result/PageResult/PageQuery 无需导入）？
- [ ] **模糊搜索是否正确使用 like/likeCast**？（String→like，非String→likeCast）
- [ ] **普通 CRUD 不使用 @Schema 注解**（仅 @OpenApi 开放接口需要）

---

## like vs likeCast 使用规范（跨数据库兼容）

> ⚠️ PostgreSQL 不支持对非字符串类型使用 LIKE 操作符，必须先转换为字符串！
> ⚠️ **此规范同时适用于 PlusLambdaQuery（查询）和 PlusLambdaUpdate（更新）！**

### 规则

| 字段类型 | 使用方法 | 说明 |
|---------|---------|------|
| `String` | `like()` | 直接模糊匹配 |
| `Long`, `Integer`, `BigDecimal` | `likeCast()` | 需要类型转换 |
| `Date`, `LocalDateTime` | `likeCast()` | 需要类型转换 |
| 其他非字符串类型 | `likeCast()` | 需要类型转换 |

### 查询场景示例

```java
// ✅ 正确：根据字段类型选择方法
lqw.and(w -> w
    .likeCast(Xxx::getId, searchValue)          // Long → likeCast
    .or().like(Xxx::getName, searchValue)       // String → like
    .or().likeCast(Xxx::getAmount, searchValue) // BigDecimal → likeCast
    .or().likeCast(Xxx::getCreateTime, searchValue) // Date → likeCast
);

// ❌ 错误：对非字符串类型使用 like（PostgreSQL 会报错）
lqw.and(w -> w
    .like(Xxx::getId, searchValue)           // Long 不能用 like！
    .or().like(Xxx::getCreateTime, searchValue) // Date 不能用 like！
);
```

### 更新场景示例

```java
// ✅ 正确：更新场景同样适用 likeCast
xxxDao.lambdaUpdate()
    .set(Xxx::getStatus, "0")
    .likeCast(Xxx::getId, "100")  // 更新 ID 包含 "100" 的记录（PostgreSQL 兼容）
    .update();

// ❌ 错误：对非字符串类型使用 like（PostgreSQL 会报错）
xxxDao.lambdaUpdate()
    .set(Xxx::getStatus, "0")
    .like(Xxx::getId, "100")  // Long 类型不能用 like！
    .update();
```

### 跨数据库实现原理

`likeCast()` 方法会根据数据库类型自动生成兼容的 SQL：
- **MySQL**: 隐式转换，无需特殊处理
- **PostgreSQL**: `CAST(column AS VARCHAR)`
- **Oracle**: `TO_CHAR(column)`
- **SQL Server**: `CAST(column AS NVARCHAR(MAX))`

---

## 批量更新（PlusLambdaUpdate）

本项目封装了 `PlusLambdaUpdate`，位于 `ruoyi-common-mybatis` 模块。

> ⚠️ **使用位置**：复杂更新逻辑应在 **DAO 层** 实现，与 `buildQueryWrapper()` 同级。

### 核心方法

| 方法 | 说明 |
|------|------|
| `set(column, value)` | 设置字段值（不忽略 null） |
| `setIfNotNull(column, value)` | 设置字段值（忽略 null/空字符串） |
| `setIncrBy(column, value)` | 字段自增 |
| `setDecrBy(column, value)` | 字段自减 |

### DAO 层实现示例

```java
// XxxDaoImpl.java
@Repository
public class XxxDaoImpl extends BaseDaoImpl<XxxMapper, Xxx> implements IXxxDao {

    /**
     * 批量更新状态
     */
    @Override
    public int updateStatus(List<Long> ids, String status) {
        return lambdaUpdate()
            .set(Xxx::getStatus, status)
            .in(Xxx::getId, ids)
            .update();
    }

    /**
     * 库存扣减（原子操作）
     */
    @Override
    public int decreaseStock(Long id, Integer count) {
        return lambdaUpdate()
            .setDecrBy(Xxx::getStock, count)
            .eq(Xxx::getId, id)
            .gt(Xxx::getStock, count)  // 库存充足才扣减
            .update();
    }
}

// XxxServiceImpl.java - Service 层只调用 DAO 方法
@Service
@RequiredArgsConstructor
public class XxxServiceImpl implements IXxxService {

    private final IXxxDao xxxDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(List<Long> ids, String status) {
        return xxxDao.updateStatus(ids, status);
    }
}
```

---

## 🔴 四层架构防火墙（硬性规则）

> **本章是四层架构的「红线规则」，违反任何一条都会导致架构混乱。**
> **每写一个 Java 类，必须先确认它在哪一层，然后遵守该层的防火墙规则。**

### 分层依赖防火墙

| 规则 | Controller | Service | DAO | Mapper |
|------|-----------|---------|-----|--------|
| **允许注入** | `IXxxService` | `IXxxDao` + 其他 `IYyyService` | 无需注入（继承 `baseMapper`） | 无 |
| **禁止注入** | `IXxxDao`、`XxxMapper`、`XxxDaoImpl` | `XxxMapper`、`XxxDaoImpl`、`XxxServiceImpl` | `IXxxService`（禁止反向依赖） | 任何 Bean |
| **允许出现的代码** | 权限注解、参数校验、`R.ok()` 响应包装 | `MapstructUtils.convert()`、`@Transactional`、业务逻辑、调用 `dao.buildQueryWrapper()` | `PlusLambdaQuery.of()`、`lambdaUpdate()`、`baseMapper.*` | XML SQL、`@Select`/`@Update` 注解 |
| **禁止出现的代码** | `PlusLambdaQuery`、`MapstructUtils`、`@Transactional`、业务逻辑 | `PlusLambdaQuery.of()`（必须调用 `dao.buildQueryWrapper`）、`new LambdaQueryWrapper` | `MapstructUtils.convert()`、`@Transactional`、`ServiceException.of()` | 业务逻辑、复杂 Java 代码 |
| **继承/实现** | 无基类 | `implements IXxxService`（**不继承**任何基类） | `extends BaseDaoImpl<XxxMapper, Xxx>` | `extends BaseMapper<Xxx>` |
| **注解** | `@RestController` + `@RequestMapping` | `@Service` + `@RequiredArgsConstructor` | `@Repository` | MyBatis `@Mapper` 扫描 |

### 代码放置决策树

```
要写的代码是什么？
│
├─ HTTP 映射（@GetMapping）、权限注解（@SaCheckPermission）、参数校验注解（@Validated）
│  └─ → Controller 层
│
├─ 业务逻辑、流程编排、调用多个 DAO/Service 协同工作
│  └─ → Service 层
│
├─ 事务控制（@Transactional）
│  └─ → Service 层（Controller 和 DAO 禁用）
│
├─ 对象转换（BO→Entity、Entity→VO）
│  └─ → Service 层（MapstructUtils.convert）
│
├─ 业务异常（ServiceException.of）
│  └─ → Service 层
│
├─ 查询条件组装（eq/like/between/and/or）
│  └─ → DAO 层的 buildQueryWrapper() 方法
│
├─ 基础 CRUD（insert/update/delete/select）
│  └─ → DAO 层（继承自 BaseDaoImpl 的方法）
│
├─ 条件更新/批量更新（lambdaUpdate + set + where）
│  └─ → DAO 层（自定义方法封装）
│
├─ 存在性判断（某条件的数据是否存在）
│  └─ → DAO 层（自定义 exists 方法）
│
├─ 复杂 SQL（多表关联、子查询、聚合统计）
│  └─ → Mapper 层（XML 或 @Select 注解）
│
└─ 跨模块数据访问
   └─ → 注入对方 IYyyService（禁止注入对方 DAO/Mapper）
```

### AI 高频违规反面教材

#### 违规 1：Service 中直接构建查询条件（最高频！）

```java
// ❌ 错误：AI 受三层架构训练数据影响，在 Service 中直接写查询
@Service
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;

    public List<OrderVo> list(OrderBo bo) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of();  // ❌ 禁止在 Service 中创建！
        lqw.eq(Order::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Order::getName, bo.getName());
        List<Order> list = orderDao.list(lqw);
        return MapstructUtils.convert(list, OrderVo.class);
    }
}

// ✅ 正确：查询条件在 DAO 层构建
@Service
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;

    public List<OrderVo> list(OrderBo bo) {
        PlusLambdaQuery<Order> wrapper = orderDao.buildQueryWrapper(bo);  // ✅ 调用 DAO
        List<Order> list = orderDao.list(wrapper);
        return MapstructUtils.convert(list, OrderVo.class);
    }
}
```

#### 违规 2：Service 继承 ServiceImpl

```java
// ❌ 错误：原版 ruoyi-vue-plus 写法，本项目禁止
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements IOrderService {
    // 继承 ServiceImpl 会让 Service 直接暴露 MyBatis-Plus API
}

// ✅ 正确：只实现接口，依赖 DAO
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;  // 通过 DAO 访问数据
}
```

#### 违规 3：Service 注入 Mapper（跳过 DAO 层）

```java
// ❌ 错误：绕过 DAO 层，破坏四层架构
@Service
public class OrderServiceImpl implements IOrderService {
    private final OrderMapper orderMapper;  // ❌ 不应直接注入 Mapper

    public Order getById(Long id) {
        return orderMapper.selectById(id);  // ❌ 跳过了 DAO 层
    }
}

// ✅ 正确：通过 DAO 层访问数据
@Service
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;  // ✅ 注入 DAO 接口

    public OrderVo get(Long id) {
        Order entity = orderDao.getById(id);  // ✅ 通过 DAO 访问
        return MapstructUtils.convert(entity, OrderVo.class);
    }
}
```

#### 违规 4：Controller 注入 DAO

```java
// ❌ 错误：Controller 直接访问 DAO，跳过 Service 层
@RestController
public class OrderController {
    private final IOrderDao orderDao;  // ❌ Controller 只能注入 Service

    @GetMapping("/getOrder/{id}")
    public R<Order> getOrder(@PathVariable Long id) {
        return R.ok(orderDao.getById(id));  // ❌ 缺少 Service 层的业务逻辑
    }
}

// ✅ 正确：Controller 只注入 Service
@RestController
public class OrderController {
    private final IOrderService orderService;  // ✅ 只注入 Service

    @GetMapping("/getOrder/{id}")
    public R<OrderVo> getOrder(@PathVariable Long id) {
        return R.ok(orderService.get(id));  // ✅ 通过 Service 调用
    }
}
```

#### 违规 5：DAO 中做对象转换

```java
// ❌ 错误：对象转换是 Service 层的职责
@Repository
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> implements IOrderDao {
    public OrderVo getVoById(Long id) {
        Order entity = getById(id);
        return MapstructUtils.convert(entity, OrderVo.class);  // ❌ DAO 不做转换
    }
}

// ✅ 正确：DAO 只返回 Entity，Service 负责转换
// OrderDaoImpl.java - DAO 层
@Repository
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> implements IOrderDao {
    // getById() 继承自 BaseDaoImpl，返回 Entity
}

// OrderServiceImpl.java - Service 层
@Service
public class OrderServiceImpl implements IOrderService {
    public OrderVo get(Long id) {
        Order entity = orderDao.getById(id);
        return MapstructUtils.convert(entity, OrderVo.class);  // ✅ Service 层转换
    }
}
```

#### 违规 6：Mapper 继承 BaseMapperPlus

```java
// ❌ 错误：原版 ruoyi-vue-plus 的 Mapper 基类
public interface OrderMapper extends BaseMapperPlus<Order> { }

// ✅ 正确：本项目只用标准 BaseMapper
public interface OrderMapper extends BaseMapper<Order> { }
```

### 边界场景指导

#### 场景 1：跨模块数据访问

> **「跨模块」= 跨 Maven 模块**（如 ruoyi-business ↔ ruoyi-system ↔ ruoyi-mall）。
> 同一个 Maven 模块内的子包之间可以直接注入 DAO（如 `SysTenantServiceImpl` 注入 `ISysUserDao`、`ISysDeptDao` 等 11 个同模块 DAO 是合规的）。

```java
// ✅ 正确：跨 Maven 模块注入对方的 Service 接口
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;
    private final IUserService userService;  // ✅ 注入其他 Maven 模块的 Service 接口

    public OrderVo get(Long id) {
        Order entity = orderDao.getById(id);
        OrderVo vo = MapstructUtils.convert(entity, OrderVo.class);
        // ✅ 通过 Service 获取关联数据
        UserVo user = userService.get(entity.getUserId());
        vo.setUserName(user.getUserName());
        return vo;
    }
}

// ✅ 正确：同 Maven 模块内可直接注入 DAO（ruoyi-system 内部）
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl implements ISysTenantService {
    private final ISysTenantDao tenantDao;
    private final ISysUserDao userDao;   // ✅ 同属 ruoyi-system，允许
    private final ISysDeptDao deptDao;   // ✅ 同属 ruoyi-system，允许
}

// ❌ 错误：跨 Maven 模块直接注入对方的 DAO（ruoyi-business → ruoyi-system）
@Service
public class OrderServiceImpl implements IOrderService {
    private final IUserDao userDao;  // ❌ 禁止跨 Maven 模块直接注入 DAO
}
```

#### 场景 2：DAO 层自定义查询方法

```java
// ✅ 正确：在 DAO 接口声明，实现类中用 baseMapper/PlusLambdaQuery 实现
// IOrderDao.java
public interface IOrderDao extends IBaseDao<Order> {
    PlusLambdaQuery<Order> buildQueryWrapper(OrderBo bo);
    boolean existsByOrderNo(String orderNo);  // ✅ 自定义查询封装在 DAO
    List<Order> findByUserId(Long userId);    // ✅ 自定义查询封装在 DAO
}

// OrderDaoImpl.java
@Repository
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> implements IOrderDao {
    @Override
    public boolean existsByOrderNo(String orderNo) {
        return baseMapper.exists(PlusLambdaQuery.<Order>of().eq(Order::getOrderNo, orderNo));
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return list(PlusLambdaQuery.<Order>of().eq(Order::getUserId, userId));
    }
}

// ❌ 错误：Service 中直接构建临时查询
@Service
public class OrderServiceImpl implements IOrderService {
    public boolean checkOrderNoExists(String orderNo) {
        // ❌ Service 不应该直接构建 PlusLambdaQuery
        return orderDao.exists(PlusLambdaQuery.<Order>of().eq(Order::getOrderNo, orderNo));
    }
}
```

#### 场景 3：lambdaUpdate 的使用边界

```java
// ✅ 推荐：复杂更新封装在 DAO 层
// IOrderDao.java
int updateStatus(List<Long> ids, String status);
int decreaseStock(Long goodsId, Integer count);

// OrderDaoImpl.java
@Override
public int updateStatus(List<Long> ids, String status) {
    return lambdaUpdate()
        .set(Order::getStatus, status)
        .in(Order::getId, ids)
        .update();
}

// ✅ 也允许：Service 中调用 DAO 暴露的 lambdaUpdate()（仅限极简单的场景）
// 例如：只有 1 个 set + 1 个 where 的极简更新
orderDao.lambdaUpdate()
    .set(Order::getRemark, "已处理")
    .eq(Order::getId, id)
    .update();

// ❌ 不推荐：Service 中写复杂的 lambdaUpdate
// 超过 2 个 set 或条件复杂时，应该封装在 DAO 方法中
orderDao.lambdaUpdate()
    .set(Order::getStatus, "3")
    .set(Order::getPayTime, new Date())
    .set(Order::getPayAmount, amount)
    .eq(Order::getId, id)
    .eq(Order::getStatus, "0")
    .update();
// ↑ 应该封装为 orderDao.confirmPay(id, amount)
```

#### 场景 4：需要自定义 SQL

```java
// ✅ 正确：在 Mapper 中定义 SQL，DAO 中调用
// OrderMapper.java
@Select("SELECT SUM(total_amount) FROM m_order WHERE user_id = #{userId} AND status = '3'")
BigDecimal sumAmountByUserId(@Param("userId") Long userId);

// OrderDaoImpl.java
public BigDecimal sumAmountByUserId(Long userId) {
    return baseMapper.sumAmountByUserId(userId);  // ✅ DAO 调用 Mapper
}

// OrderServiceImpl.java
public BigDecimal getUserTotalAmount(Long userId) {
    return orderDao.sumAmountByUserId(userId);  // ✅ Service 调用 DAO
}
```

### 分层违规自检清单

> **每个 Java 类写完后，必须执行以下自检：**

```
1. 这个类在哪一层？
   □ Controller  □ Service  □ DAO  □ Mapper  □ Domain

2. 依赖注入方向检查：
   □ Controller 是否只注入了 IXxxService？（不含 DAO/Mapper/Impl）
   □ ServiceImpl 是否只注入了 IXxxDao 和其他 IYyyService？（不含 Mapper/Impl）
   □ DaoImpl 是否只通过 baseMapper 访问数据？（不注入其他 Bean）

3. 代码位置检查：
   □ PlusLambdaQuery.of() 是否只出现在 DAO 层？
   □ MapstructUtils.convert() 是否只出现在 Service 层？
   □ @Transactional 是否只出现在 Service 层？
   □ buildQueryWrapper() 是否定义在 DAO 层？
   □ ServiceException.of() 是否只出现在 Service 层？

4. 继承/实现检查：
   □ ServiceImpl 是否不继承任何基类？（只 implements 接口）
   □ DaoImpl 是否继承 BaseDaoImpl？
   □ Mapper 是否只继承 BaseMapper？（不是 BaseMapperPlus）

5. 跨 Maven 模块检查：
   □ 跨 Maven 模块（ruoyi-business ↔ ruoyi-system ↔ ruoyi-mall）访问数据时，是否通过对方 IXxxService？
   □ 同 Maven 模块内的子包之间可以直接注入 DAO（无需通过 Service）
```

---

## 🔗 关联技能边界

本技能产出 **CRUD 全栈数据通道**，但**不**覆盖页面 UI：

| 产出物 | 是否本技能负责 | 应激活的技能 |
|--------|-------------|------------|
| 后端 Controller / Service / DAO / Mapper / Entity / BO / VO | ✅ 是 | 仅 `crud-development` |
| 前端 `xxxApi.ts` + `xxxTypes.ts` | ✅ 是 | 仅 `crud-development` |
| 前端 Vue 页面（`xxx.vue`，列表/表单/详情等） | ❌ 否 | `ui-pc` |
| 移动端页面（小程序 / H5 / APP） | ❌ 否 | `ui-mobile`（plus-uniapp / plus-app 通用） |
| 移动端页面设计（间距、布局、留白） | ❌ 否 | `ui-design-mobile` |
| 建表 SQL / 字典数据 / 菜单 | ❌ 否 | `database-ops` |

### 完整全栈开发流程

```
1. database-ops      → 建表 + 字典 + 菜单
2. crud-development  → 后端四层 + 前端 API/Types（本技能）
3. ui-pc / ui-mobile → 基于 API/Types 编写页面 UI（对照 ad.vue 模仿）
```

### 为什么拆分？

- **页面 UI 是视觉密集型**：列表 / 树形 / 详情 / 向导布局差异大，难用一套模板覆盖
- **API/Types 是高度模板化**：和 Controller 强绑定，由本技能产出更准确
- **降低单次激活成本**：纯后端任务无需加载 ui-pc 的大量组件文档
