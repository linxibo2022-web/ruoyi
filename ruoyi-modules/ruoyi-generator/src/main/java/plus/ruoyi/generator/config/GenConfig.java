package plus.ruoyi.generator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取代码生成相关配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "gen")
@PropertySource(value = {"classpath:generator.yml"}, encoding = "UTF-8")
public class GenConfig {

    /**
     * 作者
     */
    public static String author;

    /**
     * 生成包路径
     */
    public static String packageName;

    /**
     * 自动去除表前缀，默认是false
     */
    public static boolean autoRemovePre;

    /**
     * 表前缀(类名不会包含表前缀)
     */
    public static String tablePrefix;

    /**
     * 默认生成方式：0-zip压缩包，1-自定义路径
     */
    public static String defaultGenType;

    /**
     * 后端模块名称（默认：ruoyi-business）
     */
    public static String backendModuleName;

    /**
     * 前端项目根目录（默认：plus-ui）
     */
    public static String frontendRootDir;

    /**
     * 默认菜单图标（默认：guide）
     */
    public static String menuIcon;

    /**
     * 默认菜单顺序（默认：1）
     */
    public static String menuOrder;

    /**
     * 是否自动导入菜单（默认：1-是）
     */
    public static String autoImportMenu;

    public static String getAuthor() {
        return author;
    }

    @Value("${author}")
    public void setAuthor(String author) {
        GenConfig.author = author;
    }

    public static String getPackageName() {
        return packageName;
    }

    @Value("${packageName}")
    public void setPackageName(String packageName) {
        GenConfig.packageName = packageName;
    }

    public static boolean getAutoRemovePre() {
        return autoRemovePre;
    }

    @Value("${autoRemovePre}")
    public void setAutoRemovePre(boolean autoRemovePre) {
        GenConfig.autoRemovePre = autoRemovePre;
    }

    public static String getTablePrefix() {
        return tablePrefix;
    }

    @Value("${tablePrefix}")
    public void setTablePrefix(String tablePrefix) {
        GenConfig.tablePrefix = tablePrefix;
    }

    public static String getDefaultGenType() {
        return defaultGenType;
    }

    @Value("${defaultGenType:0}")
    public void setDefaultGenType(String defaultGenType) {
        GenConfig.defaultGenType = defaultGenType;
    }

    public static String getBackendModuleName() {
        return backendModuleName;
    }

    @Value("${backendModuleName:ruoyi-business}")
    public void setBackendModuleName(String backendModuleName) {
        GenConfig.backendModuleName = backendModuleName;
    }

    public static String getFrontendRootDir() {
        return frontendRootDir;
    }

    @Value("${frontendRootDir:plus-ui}")
    public void setFrontendRootDir(String frontendRootDir) {
        GenConfig.frontendRootDir = frontendRootDir;
    }

    public static String getMenuIcon() {
        return menuIcon;
    }

    @Value("${menuIcon:guide}")
    public void setMenuIcon(String menuIcon) {
        GenConfig.menuIcon = menuIcon;
    }

    public static String getMenuOrder() {
        return menuOrder;
    }

    @Value("${menuOrder:1}")
    public void setMenuOrder(String menuOrder) {
        GenConfig.menuOrder = menuOrder;
    }

    public static String getAutoImportMenu() {
        return autoImportMenu;
    }

    @Value("${autoImportMenu:1}")
    public void setAutoImportMenu(String autoImportMenu) {
        GenConfig.autoImportMenu = autoImportMenu;
    }
}
