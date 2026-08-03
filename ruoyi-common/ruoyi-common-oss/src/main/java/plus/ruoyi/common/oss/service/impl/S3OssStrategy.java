package plus.ruoyi.common.oss.service.impl;

import cn.hutool.core.io.IoUtil;
import lombok.Getter;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.common.oss.dto.OssFileInfo;
import plus.ruoyi.common.oss.dto.OssFileMetadata;
import plus.ruoyi.common.oss.dto.UploadResult;
import plus.ruoyi.common.oss.exception.OssException;
import plus.ruoyi.common.oss.entity.OssClientConfig;
import plus.ruoyi.common.oss.service.OssStrategy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * S3 存储策略实现
 * 支持所有兼容 S3 协议的云存储服务
 *
 * @author 抓蛙师
 */
public class S3OssStrategy implements OssStrategy {

    /**
     * 服务商
     */
    private final String configKey;

    /**
     * OSS 客户端配置
     */
    @Getter
    private final OssClientConfig ossClientConfig;

    /**
     * Amazon S3 异步客户端
     */
    private final S3AsyncClient client;

    /**
     * 用于管理 S3 数据传输的高级工具
     */
    private final S3TransferManager transferManager;

    /**
     * AWS S3 预签名 URL 的生成器
     */
    private final S3Presigner presigner;

    /**
     * 构造方法
     *
     * @param configKey     配置键
     * @param ossClientConfig OSS 客户端配置
     */
    public S3OssStrategy(String configKey, OssClientConfig ossClientConfig) {
        this.configKey = configKey;
        this.ossClientConfig = ossClientConfig;
        try {
            // 创建 AWS 认证信息
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(ossClientConfig.getAccessKey(), ossClientConfig.getSecretKey()));

            // MinIO 使用 HTTPS 限制使用域名访问，站点填域名。需要启用路径样式访问
            boolean isStyle = !StringUtils.containsAny(ossClientConfig.getEndpoint(), OssConstant.CLOUD_SERVICE);

            // 创建AWS基于 Netty 的 S3 客户端
            this.client = S3AsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(getEndpoint()))
                .region(of())
                .forcePathStyle(isStyle)
                .httpClient(NettyNioAsyncHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(60)).build())
                .build();

            //AWS基于 CRT 的 S3 AsyncClient 实例用作 S3 传输管理器的底层客户端
            this.transferManager = S3TransferManager.builder().s3Client(this.client).build();

            // 创建 S3 配置对象
            S3Configuration config = S3Configuration.builder().chunkedEncodingEnabled(false)
                .pathStyleAccessEnabled(isStyle).build();

            // 创建 预签名 URL 的生成器 实例，用于生成 S3 预签名 URL
            this.presigner = S3Presigner.builder()
                .region(of())
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(getEndpoint()))
                .serviceConfiguration(config)
                .build();

            // 确保存储桶存在
            ensureBucketExists();

        } catch (Exception e) {
            if (e instanceof OssException) {
                throw e;
            }
            throw new OssException("配置错误! 请检查系统配置:[" + e.getMessage() + "]");
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            String bucketName = ossClientConfig.getBucketName();
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

            try {
                // 检查存储桶是否存在
                client.headBucket(headBucketRequest).join();
            } catch (Exception e) {
                // 存储桶不存在，创建存储桶
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
                client.createBucket(createBucketRequest).join();
            }
        } catch (Exception e) {
            throw new OssException("创建Bucket失败, 请核对配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 上传文件到 Amazon S3，并返回上传结果
     *
     * @param file        本地文件
     * @param key         在 Amazon S3 中的对象键
     * @param md5Digest   本地文件的 MD5 哈希值（可选）
     * @param contentType 文件内容类型
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    @Override
    public UploadResult uploadFile(File file, String key, String md5Digest, String contentType) {
        Path filePath = file.toPath();
        try {
            // 构建上传请求对象
            FileUpload fileUpload = transferManager.uploadFile(
                x -> x.putObjectRequest(
                        y -> y.bucket(ossClientConfig.getBucketName())
                            .key(key)
                            .contentMD5(StringUtils.isNotEmpty(md5Digest) ? md5Digest : null)
                            .contentType(contentType)
                            // 用于设置对象的访问控制列表（ACL）。不同云厂商对ACL的支持和实现方式有所不同，
                            // 因此根据具体的云服务提供商，你可能需要进行不同的配置（自行开启，阿里云有acl权限配置，腾讯云没有acl权限配置）
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    .addTransferListener(LoggingTransferListener.create())
                    .source(filePath).build());

            // 等待上传完成并获取上传结果
            CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
            String eTag = uploadResult.response().eTag();

            // 提取上传结果中的 ETag，并构建一个自定义的 UploadResult 对象
            return UploadResult.builder()
                .url(getBaseUrl() + StringUtils.SLASH + key)
                .fileName(key)
                .fileSize(file.length())
                .eTag(eTag)
                .build();
        } catch (Exception e) {
            // 捕获异常并抛出自定义异常
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        } finally {
            // 无论上传是否成功，最终都会删除临时文件
            FileUtils.del(filePath);
        }
    }

    /**
     * 上传 InputStream 到 Amazon S3
     *
     * @param inputStream 要上传的输入流
     * @param key         在 Amazon S3 中的对象键
     * @param length      输入流的长度
     * @param contentType 文件内容类型
     * @return UploadResult 包含上传后的文件信息
     * @throws OssException 如果上传失败，抛出自定义异常
     */
    @Override
    public UploadResult uploadStream(InputStream inputStream, String key, Long length, String contentType) {
        // 如果输入流不是 ByteArrayInputStream，则将其读取为字节数组再创建 ByteArrayInputStream
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream = new ByteArrayInputStream(IoUtil.readBytes(inputStream));
        }
        try {
            // 创建异步请求体（length如果为空会报错）
            BlockingInputStreamAsyncRequestBody body = BlockingInputStreamAsyncRequestBody.builder()
                .contentLength(length)
                .subscribeTimeout(Duration.ofSeconds(120))
                .build();

            // 使用 transferManager 进行上传
            Upload upload = transferManager.upload(
                x -> x.requestBody(body)
                    .putObjectRequest(
                        y -> y.bucket(ossClientConfig.getBucketName())
                            .key(key)
                            .contentType(contentType)
                            // 用于设置对象的访问控制列表（ACL）。不同云厂商对ACL的支持和实现方式有所不同，
                            // 因此根据具体的云服务提供商，你可能需要进行不同的配置（自行开启，阿里云有acl权限配置，腾讯云没有acl权限配置）
                            //.acl(getAccessPolicy().getObjectCannedACL())
                            .build())
                    .build());

            // 将输入流写入请求体
            body.writeInputStream(inputStream);

            // 等待文件上传操作完成
            CompletedUpload uploadResult = upload.completionFuture().join();
            String eTag = uploadResult.response().eTag();

            // 提取上传结果中的 ETag，并构建一个自定义的 UploadResult 对象
            return UploadResult.builder()
                .url(getBaseUrl() + StringUtils.SLASH + key)
                .fileName(key)
                .fileSize(length)
                .eTag(eTag)
                .build();
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 下载文件从 Amazon S3 到临时目录
     *
     * @param path 文件在 Amazon S3 中的对象键
     * @return 下载后的文件在本地的临时路径
     * @throws OssException 如果下载失败，抛出自定义异常
     */
    @Override
    public Path downloadToTempFile(String path) {
        try {
            // 构建临时文件
            Path tempFilePath = FileUtils.createTempFile().toPath();
            // 使用 S3TransferManager 下载文件
            FileDownload downloadFile = transferManager.downloadFile(
                x -> x.getObjectRequest(
                        y -> y.bucket(ossClientConfig.getBucketName())
                            .key(removeBaseUrl(path))
                            .build())
                    .addTransferListener(LoggingTransferListener.create())
                    .destination(tempFilePath)
                    .build());
            // 等待文件下载操作完成
            downloadFile.completionFuture().join();
            return tempFilePath;
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 下载文件从 Amazon S3 到 输出流
     *
     * @param key      文件在 Amazon S3 中的对象键
     * @param out      输出流
     * @param consumer 自定义处理逻辑
     * @throws OssException 如果下载失败，抛出自定义异常
     */
    @Override
    public void downloadToStream(String key, OutputStream out, Consumer<Long> consumer) {
        try {
            // 构建下载请求
            DownloadRequest<ResponseInputStream<GetObjectResponse>> downloadRequest = DownloadRequest.builder()
                // 文件对象
                .getObjectRequest(y -> y.bucket(ossClientConfig.getBucketName())
                    .key(key)
                    .build())
                .addTransferListener(LoggingTransferListener.create())
                // 使用订阅转换器
                .responseTransformer(AsyncResponseTransformer.toBlockingInputStream())
                .build();
            // 使用 S3TransferManager 下载文件
            Download<ResponseInputStream<GetObjectResponse>> responseFuture = transferManager.download(downloadRequest);
            // 输出到流中
            try (ResponseInputStream<GetObjectResponse> responseStream = responseFuture.completionFuture().join().result()) { // auto-closeable stream
                if (consumer != null) {
                    consumer.accept(responseStream.response().contentLength());
                }
                responseStream.transferTo(out); // 阻塞调用线程 blocks the calling thread
            }
        } catch (Exception e) {
            throw new OssException("文件下载失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 删除云存储服务中指定路径下文件
     *
     * @param path 指定路径
     */
    @Override
    public void deleteFile(String path) {
        try {
            client.deleteObject(
                x -> x.bucket(ossClientConfig.getBucketName())
                    .key(removeBaseUrl(path))
                    .build()).join();
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
            sourcePath = removeBaseUrl(sourcePath);
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(ossClientConfig.getBucketName())
                .sourceKey(sourcePath)
                .destinationBucket(ossClientConfig.getBucketName())
                .destinationKey(targetPath)
                .build();

            client.copyObject(copyObjectRequest).join();
        } catch (Exception e) {
            throw new OssException("复制文件失败，请检查配置信息:[" + e.getMessage() + "]");
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
            String key = removeBaseUrl(path);
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(ossClientConfig.getBucketName())
                .key(key)
                .build();

            HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest).join();

            // 构建用户自定义元数据
            Map<String, String> userMetadata = new HashMap<>(headObjectResponse.metadata());

            return OssFileMetadata.builder()
                .fileName(key.substring(key.lastIndexOf('/') + 1))
                .filePath(key)
                .fileSize(headObjectResponse.contentLength())
                .contentType(headObjectResponse.contentType())
                .eTag(headObjectResponse.eTag())
                .createTime(Date.from(headObjectResponse.lastModified()))
                .updateTime(Date.from(headObjectResponse.lastModified()))
                .userMetadata(userMetadata)
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
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(ossClientConfig.getBucketName())
                .prefix(prefix)
                .maxKeys(maxResults)
                .build();

            ListObjectsV2Response listObjectsResponse = client.listObjectsV2(listObjectsRequest).join();

            List<OssFileInfo> fileInfoList = new ArrayList<>();

            for (S3Object s3Object : listObjectsResponse.contents()) {
                String key = s3Object.key();
                boolean isDirectory = key.endsWith("/");
                String fileName = key.substring(key.lastIndexOf('/') + 1);

                if (isDirectory && StringUtils.isEmpty(fileName)) {
                    fileName = key.substring(0, key.length() - 1);
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                }

                fileInfoList.add(OssFileInfo.builder()
                    .fileName(fileName)
                    .filePath(key)
                    .fileSize(s3Object.size())
                    .isDirectory(isDirectory)
                    .updateTime(Date.from(s3Object.lastModified()))
                    .url(getBaseUrl() + StringUtils.SLASH + key)
                    .build());
            }

            return fileInfoList;
        } catch (Exception e) {
            throw new OssException("列出文件失败，错误信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 获取文件输入流
     *
     * @param path 完整文件路径
     * @return 输入流
     */
    @Override
    public InputStream getFileAsStream(String path) throws IOException {
        // 下载文件到临时目录
        Path tempFilePath = downloadToTempFile(path);
        // 创建输入流
        InputStream inputStream = Files.newInputStream(tempFilePath);
        // 删除临时文件
        FileUtils.del(tempFilePath);
        // 返回对象内容的输入流
        return inputStream;
    }

    /**
     * 获取私有URL链接
     *
     * @param objectKey   对象KEY
     * @param expiredTime 链接授权到期时间
     */
    @Override
    public String generatePresignedUrl(String objectKey, Duration expiredTime) {
        // 使用 AWS S3 预签名 URL 的生成器 获取对象的预签名 URL
        URL url = presigner.presignGetObject(
                x -> x.signatureDuration(expiredTime)
                    .getObjectRequest(
                        y -> y.bucket(ossClientConfig.getBucketName())
                            .key(objectKey)
                            .build())
                    .build())
            .url();
        return url.toString();
    }

    /**
     * 获取 S3 客户端的终端点 URL
     *
     * @return 终端点 URL
     */
    public String getEndpoint() {
        // 根据配置文件中的是否使用 HTTPS，设置协议头部
        String header = getIsHttps();
        // 拼接协议头部和终端点，得到完整的终端点 URL
        return header + ossClientConfig.getEndpoint();
    }

    /**
     * 获取 S3 客户端的终端点 URL（自定义域名）
     *
     * @return 终端点 URL
     */
    public String getDomain() {
        // 从配置中获取域名、终端点、是否使用 HTTPS 等信息
        String domain = ossClientConfig.getDomain();
        String endpoint = ossClientConfig.getEndpoint();
        String header = getIsHttps();

        // 如果是云服务商，直接返回域名或终端点
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            return StringUtils.isNotEmpty(domain) ? header + domain : header + endpoint;
        }

        // 如果是 MinIO，处理域名并返回
        if (StringUtils.isNotEmpty(domain)) {
            return domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP) ? domain : header + domain;
        }

        // 返回终端点
        return header + endpoint;
    }

    /**
     * 根据传入的 region 参数返回相应的 AWS 区域
     *
     * @return 对应的 AWS 区域对象，或者默认的广泛支持的区域（us-east-1）
     */
    public Region of() {
        //AWS 区域字符串
        String region = ossClientConfig.getRegion();
        // 如果 region 参数非空，使用 Region.of 方法创建对应的 AWS 区域对象，否则返回默认区域
        return StringUtils.isNotEmpty(region) ? Region.of(region) : Region.US_EAST_1;
    }

    /**
     * 获取云存储服务的URL
     *
     * @return 文件路径
     */
    @Override
    public String getBaseUrl() {
        String domain = ossClientConfig.getDomain();
        String endpoint = ossClientConfig.getEndpoint();
        String header = getIsHttps();
        // 云服务商直接返回
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            return header + (StringUtils.isNotEmpty(domain) ? domain : ossClientConfig.getBucketName() + "." + endpoint);
        }
        // MinIO 单独处理
        if (StringUtils.isNotEmpty(domain)) {
            // 如果 domain 以 "https://" 或 "http://" 开头
            return (domain.startsWith(Constants.HTTPS) || domain.startsWith(Constants.HTTP)) ?
                domain + StringUtils.SLASH + ossClientConfig.getBucketName() : header + domain + StringUtils.SLASH + ossClientConfig.getBucketName();
        }
        return header + endpoint + StringUtils.SLASH + ossClientConfig.getBucketName();
    }

    /**
     * 移除路径中的基础URL部分，得到相对路径
     *
     * @param path 完整的路径，包括基础URL和相对路径
     * @return 去除基础URL后的相对路径
     */
    public String removeBaseUrl(String path) {
        return path.replace(getBaseUrl() + StringUtils.SLASH, "");
    }

    /**
     * 获取是否使用 HTTPS 的配置，并返回相应的协议头部。
     *
     * @return 协议头部，根据是否使用 HTTPS 返回 "https://" 或 "http://"
     */
    public String getIsHttps() {
        return OssConstant.IS_HTTPS.equals(ossClientConfig.getIsHttps()) ? Constants.HTTPS : Constants.HTTP;
    }

    /**
     * 生成预签名上传URL
     *
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @param expiration 过期时间(秒)
     * @return 预签名URL
     */
    @Override
    public String generatePresignedUploadUrl(String objectKey, String contentType, int expiration) {
        try {
            // 构建PUT请求
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(ossClientConfig.getBucketName())
                .key(objectKey)
                .contentType(contentType)
                .build();

            // 生成预签名PUT请求
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putRequest)
                .signatureDuration(Duration.ofSeconds(expiration))
                .build();

            // 使用presigner生成预签名URL
            PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);

            return presigned.url().toString();
        } catch (Exception e) {
            throw new OssException("生成预签名上传URL失败: " + e.getMessage());
        }
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
