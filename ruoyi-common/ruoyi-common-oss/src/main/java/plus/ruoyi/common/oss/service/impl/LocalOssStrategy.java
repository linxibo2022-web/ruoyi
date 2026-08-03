package plus.ruoyi.common.oss.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.core.utils.file.FileTypeUtils;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.common.oss.dto.OssFileInfo;
import plus.ruoyi.common.oss.dto.OssFileMetadata;
import plus.ruoyi.common.oss.dto.UploadResult;
import plus.ruoyi.common.oss.entity.OssClientConfig;
import plus.ruoyi.common.oss.exception.OssException;
import jakarta.servlet.http.HttpServletRequest;
import plus.ruoyi.common.oss.service.OssStrategy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 本地存储策略实现
 *
 * @author 抓蛙师
 */
public class LocalOssStrategy implements OssStrategy {

    /**
     * 配置键
     */
    private final String configKey;

    /**
     * OSS 客户端配置
     */
    @Getter
    private final OssClientConfig ossClientConfig;

    /**
     * 构造方法
     *
     * @param configKey     配置键
     * @param ossClientConfig OSS 客户端配置
     */
    public LocalOssStrategy(String configKey, OssClientConfig ossClientConfig) {
        this.configKey = configKey;
        this.ossClientConfig = ossClientConfig;
    }

    /**
     * 上传文件
     *
     * @param file        本地文件
     * @param key         对象键
     * @param md5Digest   MD5摘要
     * @param contentType 内容类型
     * @return 上传结果
     */
    @Override
    public UploadResult uploadFile(File file, String key, String md5Digest, String contentType) {
        try {
            return localUpload(FileUtil.getInputStream(file), key, file.length(), contentType);
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        } finally {
            FileUtils.del(file);
        }
    }

    /**
     * 上传数据流
     *
     * @param inputStream 输入流
     * @param key         对象键
     * @param length      数据长度
     * @param contentType 内容类型
     * @return 上传结果
     */
    @Override
    public UploadResult uploadStream(InputStream inputStream, String key, Long length, String contentType) {
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream = new ByteArrayInputStream(IoUtil.readBytes(inputStream));
        }
        try {
            return localUpload(inputStream, key, length, contentType);
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 本地上传实现
     *
     * @param inputStream 输入流
     * @param key         对象键
     * @param size        文件大小
     * @param contentType 内容类型
     * @return 上传结果
     */
    private UploadResult localUpload(InputStream inputStream, String key, Long size, String contentType) {
        try {
            // 上传文件路径
            String uploadPath = getUploadPath();

            // 如果key已经提供，使用它
            String fileName;
            if (StringUtils.isNotEmpty(key)) {
                fileName = key;

                // 提取目录路径
                String dirPath = getParentPath(key);
                if (StringUtils.isNotEmpty(dirPath)) {
                    // 确保目录存在
                    File directory = new File(uploadPath + File.separator + dirPath);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                }
            } else {
                // 生成新的文件名
                String extension = "";
                if (StringUtils.isNotEmpty(key) && key.contains(".")) {
                    extension = key.substring(key.lastIndexOf("."));
                }

                String datePath = DateUtils.datePath();
                String uuid = IdUtil.fastSimpleUUID();

                // 构建相对路径
                String relativePath = datePath + "/" + uuid + extension;
                fileName = relativePath;

                // 确保目录存在
                File directory = new File(uploadPath + File.separator + datePath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            }

            // 完整的本地文件路径
            String filePath = uploadPath + File.separator + fileName.replace('/', File.separatorChar);

            // 使用Hutool保存文件
            File saveFile = FileUtil.writeFromStream(inputStream, filePath);

            // 设置文件类型（如果没有提供）
            if (StringUtils.isBlank(contentType)) {
                try {
                    contentType = Files.probeContentType(saveFile.toPath());
                } catch (IOException e) {
                    // 忽略异常，使用默认类型
                }

                if (StringUtils.isBlank(contentType)) {
                    contentType = "application/octet-stream";
                }
            }

            // 返回结果
            return UploadResult.builder()
                .url(getBaseUrl() + StringUtils.SLASH + fileName.replace('\\', '/'))
                .fileName(fileName.replace('\\', '/'))
                .fileSize(size != null ? size : saveFile.length())
                .build();
        } catch (Exception e) {
            throw new OssException("上传文件失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取父级路径
     *
     * @param path 文件路径
     * @return 父级路径
     */
    private String getParentPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        int lastIndex = path.lastIndexOf('/');
        if (lastIndex <= 0) {
            return "";
        }

        return path.substring(0, lastIndex);
    }

    /**
     * 获取上传路径
     *
     * @return 上传路径
     */
    private String getUploadPath() {
        return SpringUtils.getBean(AppProperties.class).getUploadPath();
    }

    /**
     * 下载文件到临时目录
     *
     * @param path 文件路径
     * @return 临时文件路径
     */
    @Override
    public Path downloadToTempFile(String path) {
        try {
            // 从URL中移除基础URL部分，获取相对路径
            String relativePath = removeBaseUrl(path);
            // 本地存储路径
            String localPath = getUploadPath() + File.separator + relativePath;

            // 创建临时文件
            Path tempFilePath = FileUtils.createTempFile().toPath();
            // 复制文件到临时目录
            FileUtil.copy(localPath, tempFilePath.toString(), true);
            return tempFilePath;
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 下载文件到输出流
     *
     * @param key      对象键
     * @param out      输出流
     * @param consumer 自定义处理逻辑
     */
    @Override
    public void downloadToStream(String key, OutputStream out, Consumer<Long> consumer) {
        try {
            // 本地存储路径 - 直接使用key作为相对路径
            String localPath = getUploadPath() + File.separator + key;
            File file = FileUtil.file(localPath);
            long fileSize = file.length();

            // 调用处理逻辑
            if (consumer != null) {
                consumer.accept(fileSize);
            }

            // 写入输出流
            FileUtil.writeToStream(file, out);
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    @Override
    public void deleteFile(String path) {
        try {
            // 移除基础URL获取相对路径
            String relativePath = removeBaseUrl(path);

            // 本地存储路径
            String localPath = getUploadPath() + File.separator + relativePath;

            // 删除文件
            FileUtil.del(localPath);
        } catch (Exception e) {
            throw new OssException("删除文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    @Override
    public void copyFile(String sourcePath, String targetPath) {
        try {
            // 移除基础URL获取相对路径
            String sourceRelativePath = removeBaseUrl(sourcePath);

            // 源文件本地路径
            String sourceLocalPath = getUploadPath() + File.separator + sourceRelativePath;

            // 目标文件本地路径
            String targetLocalPath = getUploadPath() + File.separator + targetPath;

            // 确保目标目录存在
            String targetDir = getParentPath(targetLocalPath);
            if (StringUtils.isNotEmpty(targetDir)) {
                File directory = new File(targetDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            }

            // 复制文件
            FileUtil.copy(sourceLocalPath, targetLocalPath, true);
        } catch (Exception e) {
            throw new OssException("复制文件失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取文件元数据
     *
     * @param path 文件路径
     * @return 文件元数据
     */
    @Override
    public OssFileMetadata getFileMetadata(String path) {
        try {
            // 移除基础URL获取相对路径
            String relativePath = removeBaseUrl(path);

            // 本地存储路径
            String localPath = getUploadPath() + File.separator + relativePath;

            File file = new File(localPath);

            if (!file.exists()) {
                throw new OssException("文件不存在: " + path);
            }

            Path filePath = file.toPath();
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            String fileContentType;
            try {
                fileContentType = Files.probeContentType(filePath);
                if (StringUtils.isBlank(fileContentType)) {
                    // 根据扩展名判断
                    String ext = FileNameUtil.extName(file);
                    if (FileTypeUtils.isImage(ext)) {
                        fileContentType = "image/" + ext;
                    } else {
                        fileContentType = "application/octet-stream";
                    }
                }
            } catch (IOException e) {
                fileContentType = "application/octet-stream";
            }

            return OssFileMetadata.builder()
                .fileName(file.getName())
                .filePath(path)
                .fileSize(file.length())
                .contentType(fileContentType)
                .createTime(Date.from(attrs.creationTime().toInstant()))
                .updateTime(Date.from(attrs.lastModifiedTime().toInstant()))
                .userMetadata(Collections.emptyMap())
                .build();
        } catch (Exception e) {
            throw new OssException("获取文件元数据失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 列出文件
     *
     * @param prefix     前缀
     * @param maxResults 最大结果数
     * @return 文件信息列表
     */
    @Override
    public List<OssFileInfo> listFiles(String prefix, int maxResults) {
        try {
            String basePath = getUploadPath();
            String directoryPath = basePath;

            // 如果有前缀，则添加到路径
            if (StringUtils.isNotEmpty(prefix)) {
                directoryPath = basePath + File.separator + prefix.replace('/', File.separatorChar);
            }

            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                return Collections.emptyList();
            }

            // 获取文件列表
            List<File> files = FileUtil.loopFiles(directory);

            // 限制结果数量
            if (files.size() > maxResults) {
                files = files.subList(0, maxResults);
            }

            // 转换为OssFileInfo
            return files.stream()
                .map(file -> {
                    String filePath = file.getAbsolutePath().substring(basePath.length());
                    // 替换Windows路径分隔符为URL分隔符
                    filePath = filePath.replace('\\', '/');

                    // 确保路径以/开头
                    if (!filePath.startsWith("/")) {
                        filePath = "/" + filePath;
                    }

                    try {
                        Path path = file.toPath();
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

                        String fileContentType;
                        try {
                            fileContentType = Files.probeContentType(path);
                            if (StringUtils.isBlank(fileContentType)) {
                                // 根据扩展名判断
                                String ext = FileNameUtil.extName(file);
                                if (FileTypeUtils.isImage(ext)) {
                                    fileContentType = "image/" + ext;
                                } else {
                                    fileContentType = "application/octet-stream";
                                }
                            }
                        } catch (IOException e) {
                            fileContentType = "application/octet-stream";
                        }

                        return OssFileInfo.builder()
                            .fileName(file.getName())
                            .filePath(filePath)
                            .fileSize(file.length())
                            .isDirectory(file.isDirectory())
                            .contentType(fileContentType)
                            .updateTime(Date.from(attrs.lastModifiedTime().toInstant()))
                            .url(getBaseUrl() + filePath)
                            .build();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new OssException("列出文件失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取文件内容
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    @Override
    public InputStream getFileAsStream(String path) throws Exception {
        // 移除基础URL获取相对路径
        String relativePath = removeBaseUrl(path);

        // 本地存储路径
        String localPath = getUploadPath() + File.separator + relativePath;

        // 返回文件输入流
        return FileUtil.getInputStream(localPath);
    }

    /**
     * 获取私有URL
     * 本地存储不支持私有URL，直接返回公共URL
     *
     * @param objectKey   对象键
     * @param expiredTime 过期时间
     * @return URL
     */
    @Override
    public String generatePresignedUrl(String objectKey, Duration expiredTime) {
        return getBaseUrl() + StringUtils.SLASH + objectKey;
    }

    /**
     * 获取文件URL
     *
     * @return 访问URL
     */
    @Override
    public String getBaseUrl() {
        // 非生产环境或未配置域名时返回本地地址
        if(Constants.LOCAL.equals(SpringUtils.getActiveProfile()) || StringUtils.isBlank(ossClientConfig.getDomain())) {
            return SpringUtils.getBean(AppProperties.class).getBaseApi() + OssConstant.RESOURCE_PREFIX;
        }
        if (!Constants.PROD.equals(SpringUtils.getActiveProfile()) || StringUtils.isBlank(ossClientConfig.getDomain())) {
            return Constants.HTTP + NetUtil.getLocalhostStr() + ":" + SpringUtils.getProperty("server.port") + OssConstant.RESOURCE_PREFIX;
        }

        // 生产环境处理
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return Constants.HTTP + ossClientConfig.getDomain() + OssConstant.RESOURCE_PREFIX;
        }

        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        String baseUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();

        // 配置了域名
        if (StringUtils.isNotBlank(ossClientConfig.getDomain())) {
            // 判断是否需要HTTPS
            if (OssConstant.IS_HTTPS.equals(ossClientConfig.getIsHttps())) {
                return baseUrl.replace(Constants.HTTP, Constants.HTTPS)
                    .replace(request.getServerName(), ossClientConfig.getDomain()) + OssConstant.RESOURCE_PREFIX;
            } else {
                return baseUrl.replace(request.getServerName(), ossClientConfig.getDomain()) + OssConstant.RESOURCE_PREFIX;
            }
        }

        return baseUrl + OssConstant.RESOURCE_PREFIX;
    }

    /**
     * 移除路径中的基础URL部分，得到相对路径
     *
     * @param path 完整的路径，包括基础URL和相对路径
     * @return 去除基础URL后的相对路径
     */
    private String removeBaseUrl(String path) {
        String baseUrl = getBaseUrl() + StringUtils.SLASH;
        if (path.startsWith(baseUrl)) {
            return path.substring(baseUrl.length());
        }
        return path;
    }

    /**
     * 生成预签名上传URL
     * 本地存储返回upload接口地址
     *
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @param expiration 过期时间(秒)
     * @return 预签名URL
     */
    @Override
    public String generatePresignedUploadUrl(String objectKey, String contentType, int expiration) {
        // 返回本地上传接口地址
        String baseUrl = SpringUtils.getBean(AppProperties.class).getBaseApi();
        return baseUrl + OssConstant.UPLOAD_PATH;
    }

    /**
     * 生成公共访问URL
     *
     * @param objectKey 对象键
     * @return 公共URL
     */
    @Override
    public String generatePublicUrl(String objectKey) {
        return getBaseUrl() + StringUtils.SLASH + objectKey;
    }
}
