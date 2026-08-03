package plus.ruoyi.common.test.config;

import cn.hutool.core.io.FileUtil;
import lombok.Getter;
import java.io.File;

/**
 * 测试配置类
 * <p>
 * 统一管理测试相关的配置,避免硬编码路径
 *
 * @author 抓蛙师
 */
public class TestConfig {

    /**
     * 测试临时文件根目录
     * <p>所有测试生成的临时文件都应该放在这个目录下
     */
    @Getter
    private static final String TEST_TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "ruoyi-test").getPath();

    /**
     * 测试资源目录
     */
    @Getter
    private static final String TEST_RESOURCE_DIR = "src/test/resources";

    /**
     * 测试输出目录
     */
    @Getter
    private static final String TEST_OUTPUT_DIR = TEST_TEMP_DIR + File.separator + "output";

    /**
     * 测试上传目录
     */
    @Getter
    private static final String TEST_UPLOAD_DIR = TEST_TEMP_DIR + File.separator + "upload";

    /**
     * 初始化测试目录
     * <p>创建测试所需的目录结构
     */
    public static void initTestDirs() {
        FileUtil.mkdir(TEST_TEMP_DIR);
        FileUtil.mkdir(TEST_OUTPUT_DIR);
        FileUtil.mkdir(TEST_UPLOAD_DIR);
    }

    /**
     * 清理测试目录
     * <p>删除所有测试临时文件
     */
    public static void cleanTestDirs() {
        if (FileUtil.exist(TEST_TEMP_DIR)) {
            FileUtil.del(TEST_TEMP_DIR);
        }
    }

    /**
     * 获取测试临时文件路径
     *
     * @param fileName 文件名
     * @return 完整文件路径
     */
    public static String getTempFilePath(String fileName) {
        return TEST_TEMP_DIR + File.separator + fileName;
    }

    /**
     * 获取测试输出文件路径
     *
     * @param fileName 文件名
     * @return 完整文件路径
     */
    public static String getOutputFilePath(String fileName) {
        return TEST_OUTPUT_DIR + File.separator + fileName;
    }

    /**
     * 获取测试上传文件路径
     *
     * @param fileName 文件名
     * @return 完整文件路径
     */
    public static String getUploadFilePath(String fileName) {
        return TEST_UPLOAD_DIR + File.separator + fileName;
    }

    /**
     * 创建测试子目录
     *
     * @param subDir 子目录名称
     * @return 子目录路径
     */
    public static String createTestSubDir(String subDir) {
        String subDirPath = TEST_TEMP_DIR + File.separator + subDir;
        FileUtil.mkdir(subDirPath);
        return subDirPath;
    }
}
