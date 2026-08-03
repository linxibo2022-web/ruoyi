package plus.ruoyi.common.core.utils.file;

import cn.hutool.core.io.file.FileNameUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.StringUtils;

import java.io.File;
import java.util.Set;

/**
 * 媒体类型工具类
 * <p>
 * 提供文件类型判断、MIME类型获取等功能
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileTypeUtils {

    // MIME 类型常量
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPG = "image/jpg";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_BMP = "image/bmp";
    public static final String IMAGE_GIF = "image/gif";

    // 文件扩展名集合，用于快速查找
    public static final Set<String> IMAGE_EXTENSION = Set.of("bmp", "gif", "jpg", "jpeg", "png");
    public static final Set<String> FLASH_EXTENSION = Set.of("swf", "flv");
    public static final Set<String> MEDIA_EXTENSION = Set.of("swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg", "asf", "rm", "rmvb");
    public static final Set<String> VIDEO_EXTENSION = Set.of("mp4", "avi", "rmvb");
    public static final Set<String> DOCUMENT_EXTENSION = Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt", "pdf");
    public static final Set<String> ARCHIVE_EXTENSION = Set.of("rar", "zip", "gz", "bz2");

    // 默认允许的文件扩展名集合
    public static final Set<String> DEFAULT_ALLOWED_EXTENSION = Set.of(
        // 图片
        "bmp", "gif", "jpg", "jpeg", "png",
        // office文档
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
        // 压缩文件
        "rar", "zip", "gz", "bz2",
        // 视频格式
        "mp4", "avi", "rmvb",
        // pdf
        "pdf"
    );

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（小写，不包含点）
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 判断是否为图片文件
     *
     * @param extension 文件扩展名
     * @return 是否为图片文件
     */
    public static boolean isImage(String extension) {
        return StringUtils.isNotBlank(extension) && IMAGE_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 判断是否为媒体文件
     *
     * @param extension 文件扩展名
     * @return 是否为媒体文件
     */
    public static boolean isMedia(String extension) {
        return StringUtils.isNotBlank(extension) && MEDIA_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 判断是否为视频文件
     *
     * @param extension 文件扩展名
     * @return 是否为视频文件
     */
    public static boolean isVideo(String extension) {
        return StringUtils.isNotBlank(extension) && VIDEO_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 判断是否为文档文件
     *
     * @param extension 文件扩展名
     * @return 是否为文档文件
     */
    public static boolean isDocument(String extension) {
        return StringUtils.isNotBlank(extension) && DOCUMENT_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 判断是否为压缩文件
     *
     * @param extension 文件扩展名
     * @return 是否为压缩文件
     */
    public static boolean isArchive(String extension) {
        return StringUtils.isNotBlank(extension) && ARCHIVE_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 判断文件扩展名是否被允许
     *
     * @param extension 文件扩展名
     * @return 是否被允许
     */
    public static boolean isAllowed(String extension) {
        return StringUtils.isNotBlank(extension) && DEFAULT_ALLOWED_EXTENSION.contains(extension.toLowerCase());
    }

    /**
     * 根据文件名判断文件类型
     *
     * @param fileName 文件名
     * @return 文件类型描述
     */
    public static String getFileType(String fileName) {
        String extension = getExtension(fileName);
        if (StringUtils.isBlank(extension)) {
            return "未知";
        }

        if (isImage(extension)) {
            return "图片";
        } else if (isVideo(extension)) {
            return "视频";
        } else if (isDocument(extension)) {
            return "文档";
        } else if (isArchive(extension)) {
            return "压缩包";
        } else if (isMedia(extension)) {
            return "媒体";
        } else {
            return "其他";
        }
    }

    // ==================== File 对象相关方法 ====================

    /**
     * 判断 File 是否为图片文件
     *
     * @param file 文件对象
     * @return 是否为图片文件
     */
    public static boolean isImage(File file) {
        if (file == null) {
            return false;
        }
        return isImage(FileNameUtil.extName(file));
    }

    /**
     * 判断 File 是否为媒体文件
     *
     * @param file 文件对象
     * @return 是否为媒体文件
     */
    public static boolean isMedia(File file) {
        if (file == null) {
            return false;
        }
        return isMedia(FileNameUtil.extName(file));
    }

    /**
     * 判断 File 是否为视频文件
     *
     * @param file 文件对象
     * @return 是否为视频文件
     */
    public static boolean isVideo(File file) {
        if (file == null) {
            return false;
        }
        return isVideo(FileNameUtil.extName(file));
    }

    /**
     * 判断 File 是否为文档文件
     *
     * @param file 文件对象
     * @return 是否为文档文件
     */
    public static boolean isDocument(File file) {
        if (file == null) {
            return false;
        }
        return isDocument(FileNameUtil.extName(file));
    }

    /**
     * 判断 File 是否为压缩文件
     *
     * @param file 文件对象
     * @return 是否为压缩文件
     */
    public static boolean isArchive(File file) {
        if (file == null) {
            return false;
        }
        return isArchive(FileNameUtil.extName(file));
    }

    /**
     * 判断 File 扩展名是否被允许
     *
     * @param file 文件对象
     * @return 是否被允许
     */
    public static boolean isAllowed(File file) {
        if (file == null) {
            return false;
        }
        return isAllowed(FileNameUtil.extName(file));
    }

    /**
     * 根据 File 对象判断文件类型
     *
     * @param file 文件对象
     * @return 文件类型描述
     */
    public static String getFileType(File file) {
        if (file == null) {
            return "未知";
        }
        return getFileType(file.getName());
    }
}
