package plus.ruoyi.common.oss.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OSS 富文本内容工具类
 *
 * <p>处理富文本 HTML 中嵌入的 OSS 图片地址。与 {@code @SerialMap(PRESIGNED_URL)}（读取时为私有桶 URL
 * 动态重新签名）相对应，本工具负责<b>写入数据库前</b>把 URL 上携带的预签名参数（X-Amz-* 等）清洗掉，
 * 只持久化干净地址。
 *
 * <p>为什么要这样做：私有桶的预签名 URL 自带有效期（通常 1 小时），如果把整串签名直接写进富文本字段，
 * 一是数据库内容冗余且会过期，二是某些不经过 VO 序列化的展示路径会暴露已失效的签名。统一在入库前清洗，
 * 配合读取时的 {@code @SerialMap(PRESIGNED_URL)} 重新签名，即可形成「存干净、读重签」的完整闭环。
 *
 * @author 抓蛙师
 */
public class OssContentUtils {

    private OssContentUtils() {
    }

    /**
     * 匹配 HTML 标签中的 src / data-href 属性。
     * <p>前置负向断言 {@code (?<![\w-])} 用于避免把 {@code data-src} 之类属性中的 {@code src} 误匹配。
     */
    private static final Pattern ATTR_URL_PATTERN = Pattern.compile(
        "(?i)(?<![\\w-])(src|data-href)=([\"'])([^\"']*)\\2"
    );

    /**
     * 清洗富文本中 OSS 图片的预签名参数。
     *
     * <p>遍历 HTML 中所有 {@code src} / {@code data-href} 属性，凡是带签名特征的 URL（如包含
     * {@code X-Amz-Signature}），去掉其查询串只保留基础地址；非签名 URL（公开链接、外链、无查询串地址）
     * 原样保留，不做任何改动。
     *
     * @param html 富文本 HTML，允许为 {@code null} 或空串
     * @return 清洗后的 HTML；入参为 {@code null}/空时原样返回
     */
    public static String cleanPresignedUrls(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        Matcher matcher = ATTR_URL_PATTERN.matcher(html);
        StringBuilder result = new StringBuilder(html.length());
        while (matcher.find()) {
            String attr = matcher.group(1);
            String quote = matcher.group(2);
            String url = matcher.group(3);
            // 仅清洗带签名特征的 URL，公开/外链地址保持不变
            String cleanedUrl = isPresignedUrl(url) ? stripQuery(url) : url;
            String replacement = attr + "=" + quote + cleanedUrl + quote;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 判断 URL 是否携带各云服务商的预签名特征参数。
     *
     * @param url URL 地址
     * @return 命中任一签名特征返回 {@code true}，否则 {@code false}
     */
    private static boolean isPresignedUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.contains("X-Amz-Algorithm")                  // AWS S3 / MinIO
            || url.contains("X-Amz-Signature")                  // AWS S3 / MinIO
            || url.contains("X-Amz-Credential")                 // AWS S3 / MinIO
            || url.contains("OSSAccessKeyId")                   // 阿里云 OSS
            || url.contains("q-sign-algorithm")                 // 腾讯云 COS
            || url.contains("q-signature")                      // 腾讯云 COS
            || (url.contains("e=") && url.contains("token="))   // 七牛云
            || url.contains("AWSAccessKeyId")                   // AWS S3 v2
            || (url.contains("Expires") && url.contains("Signature")) // 华为云 OBS 等
            || url.contains("Signature");                       // 阿里云 OSS / 腾讯云 COS 通用
    }

    /**
     * 去除 URL 的查询串（{@code ?} 及其之后的全部内容）。
     * <p>对预签名 URL 而言，查询串即签名参数，去掉后即为可入库的干净地址。
     *
     * @param url 原始 URL
     * @return 去掉查询串后的 URL；无查询串时原样返回
     */
    private static String stripQuery(String url) {
        int idx = url.indexOf('?');
        return idx == -1 ? url : url.substring(0, idx);
    }

}
