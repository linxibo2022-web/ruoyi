package plus.ruoyi.system.oss.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.OssService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.common.oss.core.OssClient;
import plus.ruoyi.common.oss.dto.UploadResult;
import plus.ruoyi.common.oss.enums.AccessPolicyType;
import plus.ruoyi.common.oss.exception.OssException;
import plus.ruoyi.common.oss.factory.OssFactory;
import plus.ruoyi.system.oss.dao.ISysOssDao;
import plus.ruoyi.system.oss.domain.SysOss;
import plus.ruoyi.system.oss.domain.bo.SysOssBo;
import plus.ruoyi.system.oss.domain.vo.PresignedUrlVo;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;
import plus.ruoyi.system.oss.service.ISysOssDirectoryService;
import plus.ruoyi.system.oss.service.ISysOssService;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.*;

/**
 * 文件上传 服务层实现
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysOssServiceImpl implements ISysOssService, OssService {

    private final ISysOssDao ossDao;
    private final ISysOssDirectoryService ossDirectoryService;

    /**
     * 根据ID查询
     */
    @Override
    public SysOssVo get(Long ossId) {
        SysOss entity = ossDao.getById(ossId);
        return MapstructUtils.convert(entity, SysOssVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysOssVo> list(SysOssBo bo) {
        List<SysOss> entities = ossDao.list(ossDao.buildQueryWrapper(bo));
        return MapstructUtils.convert(entities, SysOssVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<SysOssVo> page(SysOssBo bo, PageQuery pageQuery) {
        PageResult<SysOss> entityPage = ossDao.page(ossDao.buildQueryWrapper(bo), pageQuery);
        return entityPage.convert(SysOssVo.class);
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysOssBo bo) {
        SysOss entity = MapstructUtils.convert(bo, SysOss.class);
        ossDao.insert(entity);
        return entity.getOssId();
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOssBo bo) {
        if (bo.getOssId() == null) {
            throw ServiceException.of("OSS ID不能为空");
        }
        if (!ossDao.exists(bo.getOssId())) {
            throw ServiceException.of("OSS文件不存在");
        }
        SysOss entity = MapstructUtils.convert(bo, SysOss.class);
        return ossDao.updateById(entity);
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
        return ossDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysOssBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysOss> entities = new ArrayList<>(boList.size());
        for (SysOssBo bo : boList) {
            SysOss entity = MapstructUtils.convert(bo, SysOss.class);
            entities.add(entity);
        }
        return ossDao.batchSave(entities);
    }

    /**
     * 删除前的业务规则校验
     */
    protected void beforeDelete(Collection<Long> ids) {
        List<SysOss> list = ossDao.listByIds(ids);
        for (SysOss sysOss : list) {
            OssClient storage = OssFactory.instance(sysOss.getService());
            storage.deleteFile(sysOss.getUrl());
        }
    }

    /**
     * 桶类型为 private 的URL 修改为临时URL时长为60分钟
     * 同时设置原始URL（不含预签名参数）用于前端存储
     * 注意：此方法作为兜底处理，防止某些字段没有使用 @SerialMap(PRESIGNED_URL) 注解
     *
     * @param oss OSS对象
     * @return oss 设置了originalUrl和处理了私有桶URL的OSS对象
     */
    @Override
    public SysOssVo matchingUrl(SysOssVo oss) {
        // 先保存原始URL（不含预签名参数），用于前端存储
        oss.setOriginalUrl(oss.getUrl());

        try {
            OssClient storage = OssFactory.instance(oss.getService());
            // 仅修改桶类型为 private 的URL，临时URL时长为60分钟
            if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
                oss.setUrl(storage.generatePresignedUrl(oss.getFileName(), Duration.ofMinutes(60)));
            }
        } catch (Exception e) {
            // OSS服务连接失败时，保留原始URL，不影响查询
            log.warn("OSS服务连接失败，无法生成临时URL，文件: {}, 错误: {}", oss.getFileName(), e.getMessage());
        }
        return oss;
    }


    /**
     * 根据一组 ossIds 获取对应的 SysOssVo 列表
     *
     * @param ossIds 一组文件在数据库中的唯一标识集合
     * @return 包含 SysOssVo 对象的列表
     */
    @Override
    public List<SysOssVo> listOssByIds(Collection<Long> ossIds) {
        List<SysOssVo> list = new ArrayList<>();
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        for (Long id : ossIds) {
            SysOssVo vo = ossService.getOssById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo));
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo);
                }
            }
        }
        return list;
    }

    /**
     * 根据一组 ossIds 获取对应文件的 URL 列表
     *
     * @param ossIds 以逗号分隔的 ossId 字符串
     * @return 以逗号分隔的文件 URL 字符串
     */
    @Override
    public String getUrlsByIds(String ossIds) {
        List<String> list = new ArrayList<>();
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        for (Long id : StringUtils.splitToList(ossIds, Convert::toLong)) {
            SysOssVo vo = ossService.getOssById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo).getUrl());
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo.getUrl());
                }
            }
        }
        return StringUtils.joinComma(list);
    }

    @Override
    public List<OssDTO> listOssByIds(String ossIds) {
        List<OssDTO> list = new ArrayList<>();
        for (Long id : StringUtils.splitToList(ossIds, Convert::toLong)) {
            SysOssVo vo = SpringUtils.getAopProxy(this).getOssById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    vo.setUrl(this.matchingUrl(vo).getUrl());
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                }
            }
        }
        return list;
    }


    /**
     * 根据 ossId 从缓存或数据库中获取 SysOssVo 对象
     *
     * @param ossId 文件在数据库中的唯一标识
     * @return SysOssVo 对象，包含文件信息
     */
    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    @Override
    public SysOssVo getOssById(Long ossId) {
        SysOss entity = ossDao.getById(ossId);
        return MapstructUtils.convert(entity, SysOssVo.class);
    }


    /**
     * 文件下载方法，支持一次性下载完整文件
     *
     * @param ossId    OSS对象ID
     * @param response HttpServletResponse对象，用于设置响应头和向客户端发送文件内容
     */
    @Override
    public void download(Long ossId, HttpServletResponse response) throws IOException {
        SysOssVo sysOss = SpringUtils.getAopProxy(this).getOssById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw ServiceException.of("文件数据不存在!");
        }
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance(sysOss.getService());
        storage.downloadToStream(sysOss.getFileName(), response.getOutputStream(), response::setContentLengthLong);
    }

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param file          要上传的 MultipartFile 对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     */
    @Override
    public SysOssVo upload(String moduleName, Long directoryId, String directoryPath, MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFileName) && originalFileName.endsWith("svg+xml")) {
            originalFileName = StringUtils.replace(originalFileName, ".svg+xml", ".svg");
        }
        String suffix = StringUtils.substring(originalFileName, originalFileName.lastIndexOf("."), originalFileName.length());
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, moduleName, file.getContentType());
        } catch (IOException e) {
            throw new OssException("上传文件失败");
        }
        // 保存文件信息
        return buildResultEntity(directoryId, directoryPath, originalFileName, suffix, storage.getConfigKey(), uploadResult);
    }

    /**
     * 上传文件到对象存储服务，并保存文件信息到数据库
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param file          要上传的文件对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     */
    @Override
    public SysOssVo upload(String moduleName, Long directoryId, String directoryPath, File file) {
        String fileName = file.getName();
        String suffix = StringUtils.substring(fileName, fileName.lastIndexOf("."), fileName.length());
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult = storage.uploadSuffix(file, suffix, moduleName);
        // 保存文件信息
        return buildResultEntity(directoryId, directoryPath, fileName, suffix, storage.getConfigKey(), uploadResult);
    }

    /**
     * 构建并保存OSS实体对象
     *
     * @param directoryId      目录id，可为空
     * @param directoryPath    目录路径
     * @param originalFileName 原始文件名
     * @param suffix           文件后缀
     * @param configKey        配置键
     * @param uploadResult     上传结果
     * @return OSS视图对象
     */
    @NotNull
    private SysOssVo buildResultEntity(Long directoryId, String directoryPath, String originalFileName, String suffix, String configKey, UploadResult uploadResult) {
        // 获取或创建目录ID
        if (ObjectUtils.isNull(directoryId)) {
            directoryId = ossDirectoryService.getOrCreateDirectoryByPath(directoryPath);
        }

        // 保存文件信息到数据库
        SysOss oss = new SysOss();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(suffix);
        oss.setFileName(uploadResult.getFileName());
        oss.setOriginalName(originalFileName);
        oss.setService(configKey);
        oss.setFileSize(uploadResult.getFileSize());
        oss.setDirectoryId(directoryId);
        ossDao.insert(oss);
        SysOssVo sysOssVo = MapstructUtils.convert(oss, SysOssVo.class);
        return this.matchingUrl(sysOssVo);
    }


    /**
     * 根据链接列表删除oss图片和数据库对应的数据
     * <p>
     * * @param urls oss图片链接列表
     */
    @Override
    public boolean deleteByUrls(String urls) {
        List<SysOss> ossList = ossDao.listByUrls(List.of(urls.split(StringUtils.SEPARATOR)));
        if (CollUtil.isEmpty(ossList)) {
            return false;
        }
        //先从oss库删除
        for (SysOss sysOss : ossList) {
            OssClient storage = OssFactory.instance(sysOss.getService());
            storage.deleteFile(sysOss.getUrl());
        }
        //从数据库删除
        return ossDao.deleteByIds(StreamUtils.toList(ossList, SysOss::getOssId)) > 0;
    }

    /**
     * 替换文件，保留原始数据库记录和完全相同的文件路径，并进行类型校验
     *
     * @param ossId 要替换的文件ID
     * @param file  新上传的文件对象
     * @return 替换后的文件信息
     * @throws ServiceException 如果替换过程中发生异常
     */
    @Override
    public SysOssVo replace(Long ossId, MultipartFile file) {
        // 1. 获取原始文件信息
        SysOss oldOss = ossDao.getById(ossId);
        if (ObjectUtil.isNull(oldOss)) {
            throw ServiceException.of("要替换的文件不存在");
        }

        // 2. 提取新文件信息
        String originalFilename = file.getOriginalFilename();
        String newSuffix = StringUtils.substring(originalFilename, originalFilename.lastIndexOf("."), originalFilename.length());

        // 3. 校验文件类型是否一致
        String oldSuffix = oldOss.getFileSuffix();
        if (!oldSuffix.equalsIgnoreCase(newSuffix)) {
            throw ServiceException.of("替换文件类型必须与原文件类型一致，原文件类型：" + oldSuffix + "，新文件类型：" + newSuffix);
        }

        try {
            // 4. 获取对应的存储客户端
            OssClient storage = OssFactory.instance(oldOss.getService());

            // 5. 删除OSS库中的原文件
            storage.deleteFile(oldOss.getFileName());

            // 6. 上传新文件，使用完全相同的原始路径
            UploadResult uploadResult;
            try {
                // 使用原始文件的完整路径，确保路径完全一致
                String exactPath = oldOss.getFileName();

                uploadResult = storage.uploadStream(new ByteArrayInputStream(file.getBytes()), exactPath, (long) file.getBytes().length, file.getContentType());
            } catch (IOException e) {
                throw ServiceException.of("文件读取失败: " + e.getMessage());
            }

            // 7. 更新原记录（只更新必要信息）
            oldOss.setOriginalName(originalFilename);
            oldOss.setUpdateTime(new Date());
            oldOss.setFileSize(file.getSize());

            // 确保URL保持一致
            if (!oldOss.getUrl().equals(uploadResult.getUrl())) {
                oldOss.setUrl(uploadResult.getUrl());
            }

            // 更新到数据库
            if (ossDao.updateById(oldOss) == 0) {
                throw ServiceException.of("更新文件记录失败");
            }

            // 8. 返回结果
            SysOssVo result = MapstructUtils.convert(oldOss, SysOssVo.class);
            return this.matchingUrl(result);
        } catch (Exception e) {
            throw ServiceException.of("替换文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据URL获取OSS文件信息
     * <p>使用缓存避免 @SerialMap(PRESIGNED_URL) 序列化时每次查询数据库
     *
     * @param url 文件URL
     * @return OSS文件对象DTO，如果未找到则返回null
     */
    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "'url:' + #url")
    @Override
    public OssDTO getOssByUrl(String url) {
        SysOss entity = ossDao.getByUrl(url);
        return MapstructUtils.convert(entity, OssDTO.class);
    }

    /**
     * 通过远程URL保存图片到OSS
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId
     * @param directoryPath 目录路径，如：/文档/办公
     * @param url           远程图片URL
     * @return 保存后的OSS文件信息
     */
    @Override
    public SysOssVo saveRemoteImageToOss(String moduleName, Long directoryId, String directoryPath, String url) {
        if (StringUtils.isBlank(url)) {
            throw ServiceException.of("远程图片URL不能为空");
        }
        try {
            // 使用临时文件保存下载的图片
            File tempFile = null;
            try {
                // 创建HTTP请求配置
                HttpRequest request = HttpUtil.createGet(url);
                // 添加常见的请求头，模拟浏览器行为
                request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                request.header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
                request.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

                // 如果是微信图片，特殊处理
                if (url.contains("qpic.cn") || url.contains("weixin.qq.com")) {
                    request.header("Referer", "https://mp.weixin.qq.com/");
                } else {
                    // 一般情况下设置来源为目标网站的域名
                    URI uri = URI.create(url);
                    request.header("Referer", uri.getScheme() + "://" + uri.getHost());
                }

                // 设置超时
                request.timeout(10000);

                // 执行请求并获取响应
                HttpResponse response = request.executeAsync();
                if (response.getStatus() != HttpStatus.HTTP_OK) {
                    throw new RuntimeException("下载图片失败，HTTP错误码: " + response.getStatus());
                }

                // 获取内容类型和文件名
                String contentType = response.header("Content-Type");
                if (StringUtils.isEmpty(contentType) || !contentType.startsWith("image/")) {
                    throw new RuntimeException("URL不是有效的图片地址");
                }

                // 从URL中提取文件名
                String fileName = getFileNameFromUrl(url, contentType);
                String suffix = "." + FileUtil.getSuffix(fileName);
                if (StringUtils.isEmpty(suffix)) {
                    // 根据内容类型确定后缀
                    suffix = getSuffixByContentType(contentType);
                }

                // 创建临时文件
                tempFile = File.createTempFile("oss_", suffix);

                // 下载文件到临时文件
                response.writeBody(tempFile);

                // 获取文件大小
                long fileSize = tempFile.length();

                // 读取文件内容并上传到OSS
                try (InputStream inputStream = new FileInputStream(tempFile)) {
                    OssClient storage = OssFactory.instance();
                    // 使用uploadSuffix方法上传
                    UploadResult uploadResult = storage.uploadSuffix(inputStream, suffix, fileSize, contentType);

                    // 返回SysOssVo对象
                    return buildResultEntity(directoryId, directoryPath, fileName, suffix, storage.getConfigKey(), uploadResult);
                }
            } finally {
                // 确保临时文件被删除
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("通过远程URL保存图片到OSS失败", e);
            throw ServiceException.of("下载图片失败: " + e.getMessage());
        }
    }

    /**
     * 根据内容类型获取文件后缀
     *
     * @param contentType 内容类型
     * @return 文件后缀
     */
    private String getSuffixByContentType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return ".bin";
        }

        contentType = contentType.toLowerCase();
        if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return ".jpg";
        } else if (contentType.contains("png")) {
            return ".png";
        } else if (contentType.contains("gif")) {
            return ".gif";
        } else if (contentType.contains("bmp")) {
            return ".bmp";
        } else if (contentType.contains("webp")) {
            return ".webp";
        } else if (contentType.contains("pdf")) {
            return ".pdf";
        } else {
            return ".bin";
        }
    }

    /**
     * 从URL中提取文件名
     *
     * @param url         URL地址
     * @param contentType 内容类型
     * @return 文件名
     */
    private String getFileNameFromUrl(String url, String contentType) {
        // 尝试从URL路径中提取文件名
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        // 移除URL参数
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }

        // 如果文件名不包含扩展名，则根据内容类型添加
        if (!fileName.contains(".") || fileName.length() > 100) {
            // 生成随机文件名
            fileName = IdUtil.fastSimpleUUID();

            // 根据内容类型添加扩展名
            String suffix = getSuffixByContentType(contentType);
            fileName += suffix;
        }
        return fileName;
    }

    /**
     * 根据目录ID获取目录名称
     *
     * @param directoryId 目录ID
     * @return 目录名称
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_OSS_DIRECTORY, key = "#directoryId")
    public String getDirectoryNameById(Long directoryId) {
        if (ObjectUtil.isNotNull(directoryId)) {
            // 委托给目录服务处理
            return ossDirectoryService.getDirectoryNameById(directoryId);
        }
        return "/未分类";
    }

    /**
     * 生成预签名上传URL
     *
     * @param fileName      文件名
     * @param fileType      文件类型
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @return 预签名URL信息
     */
    @Override
    public PresignedUrlVo generatePresignedUrl(String fileName, String fileType, String moduleName, Long directoryId, String directoryPath) {
        try {
            // 获取文件后缀
            String suffix = "";
            if (fileName.lastIndexOf(".") > -1) {
                suffix = fileName.substring(fileName.lastIndexOf("."));
            }

            // 获取OSS客户端
            OssClient storage = OssFactory.instance();

            // 生成唯一的文件键
            String fileKey = storage.generateFileKey(suffix, moduleName);

            // 检查是否支持直传（本地存储不支持）
            // 生成预签名URL (有效期10分钟)
            String presignedUrl = storage.generatePresignedUploadUrl(fileKey, fileType, 600);

            boolean isLocal = presignedUrl.contains(OssConstant.UPLOAD_PATH);

            // 构建最终的文件访问URL
            String fileUrl = isLocal ? "auto-generated" : storage.generatePublicUrl(fileKey);
            fileKey = isLocal ? fileUrl : fileKey;

            // 返回VO对象
            return PresignedUrlVo.builder()
                .presignedUrl(presignedUrl)
                .fileKey(fileKey)
                .fileUrl(fileUrl)
                .moduleName(moduleName)
                .directoryId(directoryId)
                .directoryPath(directoryPath)
                .expiration(600)
                .uploadTip("请在10分钟内完成文件上传")
                .build();

        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            throw new ServiceException("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 确认直传上传并保存记录
     *
     * @param fileName      文件名
     * @param fileKey       文件键
     * @param fileUrl       文件URL
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param fileSize      文件大小
     * @return OSS文件信息
     */
    @Override
    public SysOssVo confirmDirectUpload(String fileName, String fileKey, String fileUrl, String moduleName, Long directoryId, String directoryPath, Long fileSize) {
        try {
            // 获取文件后缀
            String suffix = "";
            if (fileName.lastIndexOf(".") > -1) {
                suffix = fileName.substring(fileName.lastIndexOf("."));
            }

            // 获取OSS客户端配置键
            OssClient storage = OssFactory.instance();
            String configKey = storage.getConfigKey();

            // 如果目录id没有传则获取或创建目录ID
            if (ObjectUtils.isNull(directoryId)) {
                directoryId = ossDirectoryService.getOrCreateDirectoryByPath(directoryPath);
            }

            // 创建OSS记录
            SysOss oss = new SysOss();
            oss.setUrl(fileUrl);
            oss.setFileSuffix(suffix);
            oss.setFileName(fileKey);
            oss.setOriginalName(fileName);
            oss.setService(configKey);
            oss.setFileSize(fileSize);
            oss.setDirectoryId(directoryId);

            // 保存到数据库
            ossDao.insert(oss);

            // 转换为VO并返回
            SysOssVo sysOssVo = MapstructUtils.convert(oss, SysOssVo.class);
            return this.matchingUrl(sysOssVo);
        } catch (Exception e) {
            log.error("确认直传上传失败", e);
            throw new ServiceException("确认上传失败: " + e.getMessage());
        }
    }

}
