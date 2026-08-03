package plus.ruoyi.common.core.utils.file;

import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件处理工具类
 *
 * 扩展 HuTool 的 FileUtil，提供文件下载相关的工具方法
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils extends FileUtil {

    /**
     * 设置文件下载的响应头
     *
     * 处理文件名编码问题，确保各浏览器兼容性
     *
     * @param response     HTTP响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) {
        // 对文件名进行百分号编码
        String percentEncodedFileName = percentEncode(realFileName);

        // 设置 Content-Disposition 头，支持多种浏览器
        String contentDispositionValue = "attachment; filename=%s;filename*=utf-8''%s"
            .formatted(percentEncodedFileName, percentEncodedFileName);

        // 设置跨域暴露的响应头
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        // 设置文件下载响应头
        response.setHeader("Content-disposition", contentDispositionValue);
        // 设置自定义下载文件名响应头
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * 将字符串进行 URL 编码，并处理空格字符
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) {
        // 使用 UTF-8 进行 URL 编码
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        // 将 '+' 替换为 '%20'，符合 RFC 3986 标准
        return encode.replaceAll("\\+", "%20");
    }
}
