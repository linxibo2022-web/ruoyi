package plus.ruoyi.system.oss.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.TreeBuildUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.system.oss.dao.ISysOssDao;
import plus.ruoyi.system.oss.dao.ISysOssDirectoryDao;
import plus.ruoyi.system.oss.domain.SysOss;
import plus.ruoyi.system.oss.domain.SysOssDirectory;
import plus.ruoyi.system.oss.domain.bo.SysOssDirectoryBo;
import plus.ruoyi.system.oss.domain.vo.SysOssDirectoryVo;
import plus.ruoyi.system.oss.service.ISysOssDirectoryService;

import java.util.*;

/**
 * OSS目录Service业务层处理
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysOssDirectoryServiceImpl implements ISysOssDirectoryService {

    private final ISysOssDirectoryDao ossDirectoryDao;
    private final ISysOssDao ossDao;

    /**
     * 根据ID查询
     */
    @Override
    public SysOssDirectoryVo get(Long directoryId) {
        SysOssDirectory entity = ossDirectoryDao.getById(directoryId);
        return MapstructUtils.convert(entity, SysOssDirectoryVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysOssDirectoryVo> list(SysOssDirectoryBo bo) {
        List<SysOssDirectory> entities = ossDirectoryDao.list(ossDirectoryDao.buildQueryWrapper(bo));
        return MapstructUtils.convert(entities, SysOssDirectoryVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<SysOssDirectoryVo> page(SysOssDirectoryBo bo, PageQuery pageQuery) {
        PageResult<SysOssDirectory> entityPage = ossDirectoryDao.page(ossDirectoryDao.buildQueryWrapper(bo), pageQuery);
        return entityPage.convert(SysOssDirectoryVo.class);
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysOssDirectoryBo bo) {
        SysOssDirectory entity = MapstructUtils.convert(bo, SysOssDirectory.class);
        beforeSave(entity);
        ossDirectoryDao.insert(entity);
        return entity.getDirectoryId();
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOssDirectoryBo bo) {
        if (bo.getDirectoryId() == null) {
            throw ServiceException.of("目录ID不能为空");
        }
        // 获取原始数据
        SysOssDirectory oldDirectory = ossDirectoryDao.getById(bo.getDirectoryId());
        if (oldDirectory == null) {
            throw ServiceException.of("目录不存在");
        }

        // 获取新数据
        SysOssDirectory newDirectory = MapstructUtils.convert(bo, SysOssDirectory.class);

        // 检查是否修改了目录名称
        boolean isNameChanged = !oldDirectory.getDirectoryName().equals(newDirectory.getDirectoryName());

        // 设置祖先和路径信息
        setAncestorsAndPath(newDirectory);

        // 更新当前目录
        int result = ossDirectoryDao.updateById(newDirectory);

        // 如果目录名称变更，需要更新所有子目录的路径
        if (result > 0 && isNameChanged) {
            updateChildrenPath(oldDirectory, newDirectory);
        }

        return result;
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return ossDirectoryDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysOssDirectoryBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysOssDirectory> entities = new ArrayList<>(boList.size());
        for (SysOssDirectoryBo bo : boList) {
            SysOssDirectory entity = MapstructUtils.convert(bo, SysOssDirectory.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return ossDirectoryDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysOssDirectory entity) {
        // 保存前数据校验 目录不允许与同一个父节点的同级目录重名
        SysOssDirectory existing = ossDirectoryDao.getByNameAndParentId(entity.getDirectoryName(), entity.getParentId());
        if (existing != null && !existing.getDirectoryId().equals(entity.getDirectoryId())) {
            throw ServiceException.of("目录名称已存在，请重新输入");
        }
        setAncestorsAndPath(entity);
    }

    /**
     * 设置目录的祖先列表和路径
     *
     * @param directory 目录实体
     */
    private void setAncestorsAndPath(SysOssDirectory directory) {
        // 如果是根目录
        if (directory.getParentId() == 0L) {
            directory.setAncestors("0");
            directory.setDirectoryPath("/" + directory.getDirectoryName());
            return;
        }

        // 获取父目录信息
        SysOssDirectory parentDir = ossDirectoryDao.getById(directory.getParentId());
        if (parentDir == null) {
            throw ServiceException.of("父目录不存在");
        }

        // 设置祖先列表: 父目录的祖先 + 父目录ID
        directory.setAncestors(parentDir.getAncestors() + "," + directory.getParentId());

        // 设置目录路径: 父目录的路径 + / + 当前目录名
        directory.setDirectoryPath(parentDir.getDirectoryPath() + "/" + directory.getDirectoryName());
    }

    /**
     * 修改目录名称时，需要更新所有子目录的路径
     *
     * @param oldDirectory 原目录信息
     * @param newDirectory 新目录信息
     */
    private void updateChildrenPath(SysOssDirectory oldDirectory, SysOssDirectory newDirectory) {
        // 查询所有子目录（包括孙子目录等）（ancestors字段包含当前目录ID的都是子目录）
        List<SysOssDirectory> children = ossDirectoryDao.listChildrenByAncestors(newDirectory.getDirectoryId().toString());

        if (CollUtil.isEmpty(children)) {
            return;
        }

        String oldPath = oldDirectory.getDirectoryPath();
        String newPath = newDirectory.getDirectoryPath();

        for (SysOssDirectory child : children) {
            // 只替换路径开头匹配的部分
            if (child.getDirectoryPath().startsWith(oldPath + "/")) {
                String relativePath = child.getDirectoryPath().substring(oldPath.length());
                child.setDirectoryPath(newPath + relativePath);
            }
        }
        // 批量更新到数据库
        ossDirectoryDao.batchUpdateById(children);
    }

    /**
     * OSS目录数据删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 删除前校验 校验是否有子目录或者文件,如果有则禁止删除目录
        List<Long> idList = new ArrayList<>(ids);
        if (ossDirectoryDao.countByParentIds(idList) > 0) {
            throw ServiceException.of("该目录下存在子目录不允许删除");
        }
        if (ossDao.countByDirectoryIds(idList) > 0) {
            throw ServiceException.of("该目录下存在文件不允许删除");
        }
    }

    @Override
    public List<Tree<Long>> getOssDirectoryTreeOptions(SysOssDirectoryBo bo) {
        List<SysOssDirectory> entities = ossDirectoryDao.list(ossDirectoryDao.buildQueryWrapper(bo));
        List<SysOssDirectoryVo> ossDirectoryVos = MapstructUtils.convert(entities, SysOssDirectoryVo.class);

        // 创建结果列表
        List<Tree<Long>> resultList = CollUtil.newArrayList();
        // 创建"全部"目录节点 - 不使用链式调用以避免返回void
        Tree<Long> allDir = new Tree<>();
        // 使用特殊ID以避免与"未分类"ID冲突
        allDir.setId(OssConstant.ALL);
        allDir.setParentId(0L);
        allDir.putExtra("label", "全部");
        // 权重低，显示在最前面
        allDir.setWeight(0);
        allDir.putExtra("directoryPath", "/全部");
        allDir.setChildren(List.of());

        resultList.add(allDir);

        // 创建"未分类"目录节点
        Tree<Long> uncategorizedDir = new Tree<>();
        // ID设为10000000000000000L
        uncategorizedDir.setId(OssConstant.UNCATEGORIZED);
        uncategorizedDir.setParentId(0L);
        uncategorizedDir.putExtra("label", "未分类");
        // 权重低，显示在第二位
        uncategorizedDir.setWeight(1);
        uncategorizedDir.putExtra("directoryPath", "/未分类");
        uncategorizedDir.setChildren(List.of());

        resultList.add(uncategorizedDir);

        // 处理常规目录节点
        if (CollUtil.isNotEmpty(ossDirectoryVos)) {
            List<Tree<Long>> trees = TreeBuildUtils.build(ossDirectoryVos, 0L, (sysOssDirectoryVo, tree) -> {
                tree.setId(sysOssDirectoryVo.getDirectoryId())
                    .setParentId(sysOssDirectoryVo.getParentId())
                    .setName(sysOssDirectoryVo.getDirectoryName())
                    .setWeight(sysOssDirectoryVo.getOrderNum())
                    .putExtra("directoryPath", sysOssDirectoryVo.getDirectoryPath());
            });
            resultList.addAll(trees);
        }

        return resultList;
    }

    @Override
    public boolean moveOss(Long directoryId, List<Long> ossIds) {
        // 批量更新OSS文件的目录ID
        List<SysOss> ossList = ossDao.listByIds(ossIds);
        for (SysOss oss : ossList) {
            oss.setDirectoryId(directoryId);
        }
        return ossDao.batchSave(ossList) > 0;
    }

    /**
     * 根据目录路径获取目录ID，不存在则创建
     * 支持多级目录自动创建，如：/文档/办公/图书
     *
     * @param directoryPath 目录路径，如：/文档/办公/图书
     * @return 目录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getOrCreateDirectoryByPath(String directoryPath) {
        // 参数校验
        if (StrUtil.isBlank(directoryPath)) {
            return null;
        }

        // 标准化路径：去掉开头和结尾的斜杠，然后在开头加上斜杠
        directoryPath = directoryPath.trim();
        if (directoryPath.equals("/")) {
            return 0L; // 根目录
        }

        // 移除末尾的斜杠（如果有的话）
        if (directoryPath.endsWith("/")) {
            directoryPath = directoryPath.substring(0, directoryPath.length() - 1);
        }

        // 确保以斜杠开头
        if (!directoryPath.startsWith("/")) {
            directoryPath = "/" + directoryPath;
        }

        // 首先尝试直接查询该路径是否已存在
        SysOssDirectory existingDirectory = ossDirectoryDao.getByDirectoryPath(directoryPath);

        if (existingDirectory != null) {
            return existingDirectory.getDirectoryId();
        }

        // 路径不存在，需要逐级创建
        String[] pathParts = directoryPath.split("/");
        // 去掉空字符串（第一个元素是空的，因为路径以/开头）
        List<String> parts = new ArrayList<>();
        for (String part : pathParts) {
            if (StrUtil.isNotBlank(part)) {
                parts.add(part);
            }
        }

        if (parts.isEmpty()) {
            return 0L; // 根目录
        }

        Long parentId = 0L; // 从根目录开始
        StringBuilder currentPathBuilder = new StringBuilder();

        // 逐级创建目录
        for (String currentDirName : parts) {
            currentPathBuilder.append("/").append(currentDirName);
            String currentPath = currentPathBuilder.toString();

            // 检查当前路径的目录是否存在
            SysOssDirectory currentDirectory = ossDirectoryDao.getByDirectoryPath(currentPath);

            if (currentDirectory != null) {
                // 目录已存在，使用现有目录ID作为下一级的父目录ID
                parentId = currentDirectory.getDirectoryId();
            } else {
                // 目录不存在，创建新目录
                SysOssDirectory newDirectory = new SysOssDirectory();
                newDirectory.setDirectoryName(currentDirName);
                newDirectory.setParentId(parentId);
                newDirectory.setDirectoryPath(currentPath);
                newDirectory.setOrderNum(0L);
                newDirectory.setStatus("0"); // 正常状态
                newDirectory.setIsDefault("0"); // 非默认目录

                // 设置祖先列表
                if (parentId == 0L) {
                    newDirectory.setAncestors("0");
                } else {
                    // 获取父目录的祖先列表
                    SysOssDirectory parentDirectory = ossDirectoryDao.getById(parentId);
                    if (parentDirectory != null) {
                        newDirectory.setAncestors(parentDirectory.getAncestors() + "," + parentId);
                    } else {
                        log.warn("父目录不存在，parentId: {}", parentId);
                        newDirectory.setAncestors("0," + parentId);
                    }
                }

                // 保存目录
                ossDirectoryDao.insert(newDirectory);

                parentId = newDirectory.getDirectoryId();
                log.info("创建目录成功：{}, ID: {}", currentPath, parentId);
            }
        }

        return parentId;
    }

    /**
     * 根据目录ID获取目录名称
     *
     * @param directoryId 目录ID
     * @return 目录名称（带/前缀），如果不存在返回null
     */
    @Override
    public String getDirectoryNameById(Long directoryId) {
        SysOssDirectory entity = ossDirectoryDao.getById(directoryId);
        if (entity != null) {
            return "/" + entity.getDirectoryName();
        }
        return null;
    }
}
