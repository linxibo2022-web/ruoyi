package plus.ruoyi.common.oss.service;

import plus.ruoyi.common.oss.dto.OssFileInfo;
import plus.ruoyi.common.oss.dto.OssFileMetadata;
import plus.ruoyi.common.oss.dto.UploadResult;
import plus.ruoyi.common.oss.entity.OssClientConfig;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * 对象存储策略接口
 * 定义存储操作的标准方法
 *
 * @author 抓蛙师
 */
public interface OssStrategy {

    /**
     * 上传文件
     *
     * @param file        本地文件
     * @param key         对象键
     * @param md5Digest   MD5摘要
     * @param contentType 内容类型
     * @return 上传结果
     */
    UploadResult uploadFile(File file, String key, String md5Digest, String contentType);

    /**
     * 上传数据流
     *
     * @param inputStream 输入流
     * @param key         对象键
     * @param length      数据长度
     * @param contentType 内容类型
     * @return 上传结果
     */
    UploadResult uploadStream(InputStream inputStream, String key, Long length, String contentType);

    /**
     * 下载文件到临时目录
     *
     * @param path 文件路径
     * @return 临时文件路径
     */
    Path downloadToTempFile(String path);

    /**
     * 下载文件到输出流
     *
     * @param key      对象键
     * @param out      输出流
     * @param consumer 自定义处理逻辑
     */
    void downloadToStream(String key, OutputStream out, Consumer<Long> consumer);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void deleteFile(String path);

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    void copyFile(String sourcePath, String targetPath);

    /**
     * 获取文件元数据
     *
     * @param path 文件路径
     * @return 文件元数据
     */
    OssFileMetadata getFileMetadata(String path);

    /**
     * 列出文件
     *
     * @param prefix     前缀
     * @param maxResults 最大结果数
     * @return 文件信息列表
     */
    List<OssFileInfo> listFiles(String prefix, int maxResults);

    /**
     * 获取文件内容
     *
     * @param path 文件路径
     * @return 文件输入流
     * @throws Exception 如果获取失败
     */
    InputStream getFileAsStream(String path) throws Exception;

    /**
     * 生成预签名URL
     *
     * @param objectKey   对象键
     * @param expiredTime 过期时间
     * @return 预签名URL
     */
    String generatePresignedUrl(String objectKey, Duration expiredTime);

    /**
     * 获取基础URL
     *
     * @return 基础URL
     */
    String getBaseUrl();

    /**
     * 获取OSS 客户端配置
     *
     * @return OSS 客户端配置
     */
    OssClientConfig getOssClientConfig();

    /**
     * 生成预签名上传URL
     *
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @param expiration 过期时间(秒)
     * @return 预签名URL
     */
    String generatePresignedUploadUrl(String objectKey, String contentType, int expiration);

    /**
     * 生成公共访问URL
     *
     * @param objectKey 对象键
     * @return 公共URL
     */
    String generatePublicUrl(String objectKey);
}
