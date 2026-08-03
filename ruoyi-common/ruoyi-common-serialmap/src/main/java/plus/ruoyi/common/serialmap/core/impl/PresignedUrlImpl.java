package plus.ruoyi.common.serialmap.core.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.core.service.OssService;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.oss.core.OssClient;
import plus.ruoyi.common.oss.enums.AccessPolicyType;
import plus.ruoyi.common.oss.factory.OssFactory;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 预签名URL转换器
 *
 * <p>为私有文件URL生成预签名访问URL，支持单个URL、多URL（逗号分隔）和富文本HTML处理
 *
 * <p>功能特性：
 * <ul>
 *   <li>智能识别私有文件路径前缀</li>
 *   <li>支持单个URL和多URL（逗号分隔）处理</li>
 *   <li>支持富文本HTML中的图片标签加签</li>
 *   <li>可配置签名有效期</li>
 *   <li>自动跳过公有文件URL</li>
 *   <li>异常情况返回原始URL</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 基础用法：默认配置（检测/private/前缀，60分钟有效期）
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)}
 * private String imageUrl;
 *
 * // 自定义有效期：通过param参数指定分钟数
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL, param = "30")}
 * private String documentUrl;
 *
 * // 富文本HTML处理：自动检测HTML标签，无需手动配置
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)}
 * private String content;
 *
 * // 富文本+自定义有效期
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL, param = "30")}
 * private String richContent;
 *
 * // 指定源字段：从其他字段获取URL路径
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL, source = "filePath")}
 * private String fileUrl;
 *
 * // 多URL处理：逗号分隔的URL字符串
 * {@code @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)}
 * private String galleryUrls; // 如："/private/1.jpg,/private/2.jpg"
 * </pre>
 *
 * @author 抓蛙师
 */
@Slf4j
@SerialMapType(type = SerialMapConstant.PRESIGNED_URL)
public class PresignedUrlImpl implements SerialMapInterface<String> {

    /**
     * 默认签名有效期（分钟）
     */
    private static final int DEFAULT_EXPIRE_MINUTES = 60;

    /**
     * HTML img标签正则表达式（匹配src和data-href属性）
     */
    private static final Pattern IMG_TAG_PATTERN = Pattern.compile(
        "<img[^>]+(src|data-href)=[\"']([^\"']+)[\"'][^>]*>",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(Object key, String param) {
        if (ObjectUtils.isNull(key)) {
            return null;
        }

        String urlString = String.valueOf(key);
        if (StringUtils.isBlank(urlString)) {
            return urlString;
        }

        // 解析有效期参数
        int expireMinutes = parseExpireMinutes(param);

        // 自动检测是否为HTML富文本
        if (ReUtil.contains("<[^>]+>", urlString)) {
            return processHtmlContent(urlString, expireMinutes);
        }

        // 处理普通URL字符串
        return processUrlString(urlString, expireMinutes);
    }

    /**
     * 解析有效期参数
     *
     * @param param 参数字符串
     * @return 有效期（分钟）
     */
    private int parseExpireMinutes(String param) {
        if (StringUtils.isBlank(param)) {
            return DEFAULT_EXPIRE_MINUTES;
        }

        try {
            int minutes = Integer.parseInt(param.trim());
            return minutes > 0 ? minutes : DEFAULT_EXPIRE_MINUTES;
        } catch (NumberFormatException e) {
            log.debug("解析有效期参数失败: {}, 使用默认值: {}", param, DEFAULT_EXPIRE_MINUTES);
            return DEFAULT_EXPIRE_MINUTES;
        }
    }

    /**
     * 处理HTML富文本内容
     *
     * @param htmlContent   HTML内容
     * @param expireMinutes 签名有效期（分钟）
     * @return 处理后的HTML内容
     */
    private String processHtmlContent(String htmlContent, int expireMinutes) {
        if (!htmlContent.contains("<img")) {
            return htmlContent;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = IMG_TAG_PATTERN.matcher(htmlContent);

        while (matcher.find()) {
            String imgTag = matcher.group(0);
            String processedTag = processImgTag(imgTag, expireMinutes);
            matcher.appendReplacement(result, Matcher.quoteReplacement(processedTag));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 处理单个img标签
     *
     * @param imgTag        img标签HTML
     * @param expireMinutes 签名有效期（分钟）
     * @return 处理后的img标签
     */
    private String processImgTag(String imgTag, int expireMinutes) {
        // 提取src属性
        String srcUrl = extractAttributeValue(imgTag, "src");
        if (StringUtils.isNotBlank(srcUrl)) {
            String signedUrl = processSingleUrl(srcUrl, expireMinutes);
            if (!srcUrl.equals(signedUrl)) {
                imgTag = imgTag.replaceFirst(
                    "src=[\"']" + Pattern.quote(srcUrl) + "[\"']",
                    "src=\"" + signedUrl + "\""
                );
            }
        }

        // 提取data-href属性
        String dataHref = extractAttributeValue(imgTag, "data-href");
        if (StringUtils.isNotBlank(dataHref)) {
            String signedUrl = processSingleUrl(dataHref, expireMinutes);
            if (!dataHref.equals(signedUrl)) {
                imgTag = imgTag.replaceFirst(
                    "data-href=[\"']" + Pattern.quote(dataHref) + "[\"']",
                    "data-href=\"" + signedUrl + "\""
                );
            }
        }

        return imgTag;
    }

    /**
     * 提取HTML属性值
     *
     * @param html          HTML字符串
     * @param attributeName 属性名称
     * @return 属性值
     */
    private String extractAttributeValue(String html, String attributeName) {
        Pattern pattern = Pattern.compile(
            attributeName + "=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 处理URL字符串（支持单个和多个URL）
     *
     * @param urlString     原始URL字符串
     * @param expireMinutes 签名有效期（分钟）
     * @return 处理后的URL字符串
     */
    private String processUrlString(String urlString, int expireMinutes) {
        if (urlString.contains(StringUtils.SEPARATOR)) {
            // 处理多个URL
            List<String> urls = StringUtils.splitToList(urlString);
            List<String> processedUrls = new ArrayList<>();

            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (StringUtils.isNotBlank(trimmedUrl)) {
                    processedUrls.add(processSingleUrl(trimmedUrl, expireMinutes));
                }
            }

            return StringUtils.joinComma(processedUrls);
        } else {
            // 处理单个URL
            return processSingleUrl(urlString.trim(), expireMinutes);
        }
    }

    /**
     * 处理单个URL
     *
     * @param url           原始URL
     * @param expireMinutes 签名有效期（分钟）
     * @return 处理后的URL
     */
    private String processSingleUrl(String url, int expireMinutes) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        // 如果URL包含预签名参数，去除旧的预签名参数
        if (isPresignedUrl(url)) {
            url = removePresignedParams(url);
            log.debug("去除旧预签名参数后的URL: {}", url);
        }

        try {
            // 查询OSS配置
            OssService ossService = SpringUtils.getBean(OssService.class);
            OssDTO ossDto = ossService.getOssByUrl(url);
            OssClient ossClient;

            if (ossDto != null && StringUtils.isNotBlank(ossDto.getService())) {
                ossClient = OssFactory.instance(ossDto.getService());
                log.debug("使用OSS配置 {} 生成预签名URL", ossDto.getService());
            } else {
                ossClient = OssFactory.instance();
                log.debug("使用默认OSS配置生成预签名URL");
            }

            // 判断是否为私有桶，如果不是则直接返回原始URL
            if (ossClient.getAccessPolicy() != AccessPolicyType.PRIVATE) {
                return url;
            }

            // 如果找到了对应的OSS记录，直接使用fileName作为对象键
            String objectKey;
            if (ossDto != null && StringUtils.isNotBlank(ossDto.getFileName())) {
                objectKey = ossDto.getFileName();
                log.debug("从OSS记录获取对象键: {}", objectKey);
            } else {
                // 如果没有找到OSS记录，说明该URL可能不是通过系统上传的，跳过处理
                return url;
            }

            // 生成预签名URL
            Duration expiration = Duration.ofMinutes(expireMinutes);

            return ossClient.generatePresignedUrl(objectKey, expiration);
        } catch (Exception e) {
            return url; // 失败时返回原始URL
        }
    }

    /**
     * 判断URL是否已经是预签名URL
     *
     * @param url URL地址
     * @return 是否为预签名URL
     */
    private boolean isPresignedUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        // 检测各云服务商的预签名URL特征参数
        return url.contains("X-Amz-Algorithm") ||           // AWS S3/MinIO
            url.contains("X-Amz-Signature") ||           // AWS S3/MinIO
            url.contains("X-Amz-Credential") ||          // AWS S3/MinIO
            url.contains("OSSAccessKeyId") ||            // 阿里云OSS
            url.contains("Signature") ||                 // 阿里云OSS/腾讯云COS
            url.contains("q-sign-algorithm") ||          // 腾讯云COS
            url.contains("q-signature") ||               // 腾讯云COS
            (url.contains("e=") && url.contains("token=")) || // 七牛云
            url.contains("AWSAccessKeyId") ||            // AWS S3 v2
            (url.contains("Expires") && url.contains("Signature")); // 华为云OBS等
    }

    /**
     * 去除URL中的预签名参数
     *
     * @param url 包含预签名参数的URL
     * @return 去除预签名参数后的URL
     */
    private String removePresignedParams(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        // 如果URL不包含查询参数，直接返回
        int questionMarkIndex = url.indexOf('?');
        if (questionMarkIndex == -1) {
            return url;
        }

        // 分离基础URL和查询参数

        // 返回不带任何查询参数的URL（因为所有参数都是预签名相关的）
        return url.substring(0, questionMarkIndex);
    }

}
