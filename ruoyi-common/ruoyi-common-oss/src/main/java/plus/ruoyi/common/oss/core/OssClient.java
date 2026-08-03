package plus.ruoyi.common.oss.core;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import plus.ruoyi.common.core.service.TenantService;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.oss.dto.OssFileInfo;
import plus.ruoyi.common.oss.dto.OssFileMetadata;
import plus.ruoyi.common.oss.dto.UploadResult;
import plus.ruoyi.common.oss.entity.OssClientConfig;
import plus.ruoyi.common.oss.enums.AccessPolicyType;
import plus.ruoyi.common.oss.exception.OssException;
import plus.ruoyi.common.oss.factory.OssStrategyFactory;
import plus.ruoyi.common.oss.service.OssStrategy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * 对象存储客户端
 * 支持 S3 兼容协议的云存储和本地文件存储
 *
 * @author 抓蛙师
 */
public class OssClient {

    /**
     * 配置键
     */
    @Getter
    private final String configKey;

    /**
     * OSS 客户端配置
     */
    @Getter
    private final OssClientConfig ossClientConfig;

    /**
     * 存储策略
     */
    private final OssStrategy strategy;

    /**
     * 构造方法
     *
     * @param configKey     配置键
     * @param ossClientConfig OSS 客户端配置
     */
    public OssClient(String configKey, OssClientConfig ossClientConfig) {
        this.configKey = configKey;
        this.ossClientConfig = ossClientConfig;
        // 使用工厂创建存储策略
        this.strategy = OssStrategyFactory.createStrategy(configKey, ossClientConfig);
    }

    /**
     * 上传文件
     *
     * @param file        本地文件
     * @param key         对象键
     * @param contentType 内容类型
     * @return 上传结果
     */
    public UploadResult uploadFile(File file, String key, String contentType) {
        return strategy.uploadFile(file, key, null, contentType);
    }

    /**
     * 上传文件，使用后缀生成对象键
     *
     * @param file   文件
     * @param suffix 后缀
     * @return 上传结果
     */
    public UploadResult uploadSuffix(File file, String suffix) {
        String contentType = FileUtils.getMimeType(suffix);
        return uploadFile(file, getPath(suffix, null), contentType);
    }

    /**
     * 上传文件，使用后缀和模块名生成对象键
     *
     * @param file       文件
     * @param suffix     后缀
     * @param moduleName 模块名称，可为空
     * @return 上传结果
     */
    public UploadResult uploadSuffix(File file, String suffix, String moduleName) {
        String contentType = FileUtils.getMimeType(suffix);
        return uploadFile(file, getPath(suffix, moduleName), contentType);
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
    public UploadResult uploadStream(InputStream inputStream, String key, Long length, String contentType) {
        return strategy.uploadStream(inputStream, key, length, contentType);
    }

    /**
     * 上传字节数组，使用后缀生成对象键
     *
     * @param data        字节数组
     * @param suffix      后缀
     * @param contentType 内容类型
     * @return 上传结果
     */
    public UploadResult uploadSuffix(byte[] data, String suffix, String contentType) {
        return uploadStream(new ByteArrayInputStream(data), getPath(suffix, null), (long) data.length, contentType);
    }

    /**
     * 上传字节数组，使用后缀和模块名生成对象键
     *
     * @param data        字节数组
     * @param suffix      后缀
     * @param moduleName  模块名称，可为空
     * @param contentType 内容类型
     * @return 上传结果
     */
    public UploadResult uploadSuffix(byte[] data, String suffix, String moduleName, String contentType) {
        return uploadStream(new ByteArrayInputStream(data), getPath(suffix, moduleName), (long) data.length, contentType);
    }

    /**
     * 上传输入流，使用后缀生成对象键
     *
     * @param inputStream 输入流
     * @param suffix      后缀
     * @param length      数据长度
     * @param contentType 内容类型
     * @return 上传结果
     */
    public UploadResult uploadSuffix(InputStream inputStream, String suffix, Long length, String contentType) {
        return uploadStream(inputStream, getPath(suffix, null), length, contentType);
    }

    /**
     * 上传输入流，使用后缀和模块名生成对象键
     *
     * @param inputStream 输入流
     * @param suffix      后缀
     * @param moduleName  模块名称，可为空
     * @param length      数据长度
     * @param contentType 内容类型
     * @return 上传结果
     */
    public UploadResult uploadSuffix(InputStream inputStream, String suffix, String moduleName, Long length, String contentType) {
        return uploadStream(inputStream, getPath(suffix, moduleName), length, contentType);
    }

    /**
     * 下载文件到临时目录
     *
     * @param path 文件路径
     * @return 临时文件路径
     */
    public Path downloadToTempFile(String path) {
        return strategy.downloadToTempFile(path);
    }

    /**
     * 下载文件到输出流
     *
     * @param key      对象键
     * @param out      输出流
     * @param consumer 自定义处理逻辑
     */
    public void downloadToStream(String key, OutputStream out, Consumer<Long> consumer) {
        strategy.downloadToStream(key, out, consumer);
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    public void deleteFile(String path) {
        strategy.deleteFile(path);
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    public void copyFile(String sourcePath, String targetPath) {
        strategy.copyFile(sourcePath, targetPath);
    }

    /**
     * 获取文件元数据
     *
     * @param path 文件路径
     * @return 文件元数据
     */
    public OssFileMetadata getFileMetadata(String path) {
        return strategy.getFileMetadata(path);
    }

    /**
     * 列出文件
     *
     * @param prefix     前缀
     * @param maxResults 最大结果数
     * @return 文件信息列表
     */
    public List<OssFileInfo> listFiles(String prefix, int maxResults) {
        return strategy.listFiles(prefix, maxResults);
    }

    /**
     * 获取文件内容
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    public InputStream getFileAsStream(String path) {
        try {
            return strategy.getFileAsStream(path);
        } catch (Exception e) {
            throw new OssException("获取文件内容失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 生成预签名URL
     *
     * @param objectKey   对象键
     * @param expiredTime 过期时间
     * @return URL
     */
    public String generatePresignedUrl(String objectKey, Duration expiredTime) {
        return strategy.generatePresignedUrl(objectKey, expiredTime);
    }

    /**
     * 获取URL
     *
     * @return URL
     */
    public String getBaseUrl() {
        return strategy.getBaseUrl();
    }

    /**
     * 生成文件存储路径
     * <p>
     * 路径结构: [{prefix}/]{tenantId}/[{moduleName}/]{datePath}/{uuid}{suffix}
     * <ul>
     *   <li>prefix: 可选的业务前缀，如 "avatar"、"banner" 等，用于业务模块分类</li>
     *   <li>tenantId: 租户标识，用于多租户数据隔离</li>
     *   <li>moduleName: 可选的模块名称，用于进一步分类文件</li>
     *   <li>datePath: 按日期分层的目录结构，如 "2024/05/27"</li>
     *   <li>uuid: 唯一标识符，避免文件名冲突</li>
     *   <li>suffix: 文件扩展名，如 ".jpg"、".png" 等</li>
     * </ul>
     *
     * <p>示例路径:</p>
     * <pre>
     * 带前缀和模块: avatar/tenant123/user/2024/05/27/abc123def456.jpg
     * 带模块无前缀: tenant123/document/2024/05/27/abc123def456.pdf
     * 无模块有前缀: avatar/tenant123/2024/05/27/abc123def456.jpg
     * 无前缀无模块: tenant123/2024/05/27/abc123def456.jpg
     * </pre>
     *
     * @param suffix     文件后缀名，必须包含点号，如 ".jpg"、".png"
     * @param moduleName 模块名称，如avatar，可为空
     * @return 完整的文件存储路径
     * @throws IllegalArgumentException 当suffix为空时抛出
     */
    private String getPath(String suffix, String moduleName) {
        // 获取业务配置的路径前缀（可选），用于业务模块分类
        String prefix = ossClientConfig.getPrefix();

        // 获取当前租户ID，用于实现多租户数据隔离
        String tenantId = SpringUtils.getBean(TenantService.class).getTenantId();

        // 生成基于当前日期的分层目录，格式: yyyy/MM/dd
        String datePath = DateUtils.datePath();

        // 生成32位简化UUID，确保文件名唯一性
        String uuid = IdUtil.fastSimpleUUID();

        // 使用StringBuilder高效构建路径
        StringBuilder pathBuilder = new StringBuilder();

        // 条件拼接：仅在配置了前缀时添加业务模块分类路径
        if (StrUtil.isNotBlank(prefix)) {
            pathBuilder.append(prefix).append("/");
        }

        // 拼接租户ID
        pathBuilder.append(tenantId).append("/");

        // 条件拼接：仅在指定了模块名称时添加模块路径
        if (StrUtil.isNotBlank(moduleName)) {
            pathBuilder.append(moduleName).append("/");
        }

        // 拼接标准路径组件：日期目录 + 唯一文件名 + 扩展名
        pathBuilder.append(datePath)
            .append("/").append(uuid)
            .append(suffix);

        return pathBuilder.toString();
    }

    /**
     * 生成文件键，用于生成预签名URL等场景
     *
     * @param suffix     文件后缀
     * @param moduleName 模块名称，可为空
     * @return 文件键
     */
    public String generateFileKey(String suffix, String moduleName) {
        return getPath(suffix, moduleName);
    }

    /**
     * 检查配置是否相同
     *
     * @param ossClientConfig OSS 客户端配置
     * @return 是否相同
     */
    public boolean checkOssClientConfigSame(OssClientConfig ossClientConfig) {
        return this.ossClientConfig.equals(ossClientConfig);
    }

    /**
     * 获取当前桶权限类型
     *
     * @return 当前桶权限类型
     */
    public AccessPolicyType getAccessPolicy() {
        return AccessPolicyType.getByType(ossClientConfig.getAccessPolicy());
    }

    /**
     * 生成预签名上传URL
     *
     * @param objectKey   对象键
     * @param contentType 内容类型
     * @param expiration  过期时间(秒)
     * @return 预签名URL
     */
    public String generatePresignedUploadUrl(String objectKey, String contentType, int expiration) {
        return strategy.generatePresignedUploadUrl(objectKey, contentType, expiration);
    }

    /**
     * 生成公共访问URL
     *
     * @param objectKey 对象键
     * @return 公共URL
     */
    public String generatePublicUrl(String objectKey) {
        return strategy.generatePublicUrl(objectKey);
    }
}
