package plus.ruoyi.system.core.domain.vo;

import cn.hutool.core.lang.Validator;
import lombok.Data;

/**
 * 路由显示信息
 * 用于定义路由在前端展示的各种元数据属性
 *
 * @author ruoyi
 */
@Data
public class MetaVo {

    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     * 作为默认显示文本，当没有对应的国际化翻译时会使用此值
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     * 用于在菜单项前显示的图标
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     * 用于控制页面是否需要保持状态，默认为false（开启缓存）
     */
    private boolean noCache;

    /**
     * 内链地址（http(s)://开头）
     * 当设置此值时，点击菜单将打开链接而不是加载组件
     */
    private String link;

    /**
     * 国际化键
     * 用于前端国际化翻译，优先级高于title
     * 命名规则通常为：menu.模块.功能 或 button.操作类型
     */
    private String i18nKey;

    /**
     * 无参构造函数
     */
    public MetaVo() {
    }

    /**
     * 基础构造函数
     *
     * @param title 路由显示名称
     * @param icon 图标
     */
    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    /**
     * 包含缓存设置的构造函数
     *
     * @param title 路由显示名称
     * @param icon 图标
     * @param noCache 是否禁用缓存
     */
    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    /**
     * 包含链接地址的构造函数
     *
     * @param title 路由显示名称
     * @param icon 图标
     * @param link 链接地址
     */
    public MetaVo(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        if (Validator.isUrl(link)) {
            this.link = link;
        }
    }

    /**
     * 包含缓存设置和链接地址的构造函数
     *
     * @param title 路由显示名称
     * @param icon 图标
     * @param noCache 是否禁用缓存
     * @param link 链接地址（只有当是http(s)开头的链接时才会设置）
     */
    public MetaVo(String title, String icon, boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (Validator.isUrl(link)) {
            this.link = link;
        }
    }

    /**
     * 完整构造函数，包含国际化键
     *
     * @param title 路由显示名称（当没有对应国际化翻译时使用）
     * @param icon 图标
     * @param noCache 是否禁用缓存
     * @param link 链接地址（只有当是http(s)开头的链接时才会设置）
     * @param i18nKey 国际化键名，用于前端翻译（格式通常为：menu.模块.功能）
     */
    public MetaVo(String title, String icon, boolean noCache, String link, String i18nKey) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (Validator.isUrl(link)) {
            this.link = link;
        }
        this.i18nKey = i18nKey;
    }
}
