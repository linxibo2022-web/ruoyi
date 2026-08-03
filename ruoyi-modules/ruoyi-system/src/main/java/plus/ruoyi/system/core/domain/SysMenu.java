package plus.ruoyi.system.core.domain;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限表 sys_menu
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 菜单ID
     */
    @TableId(value = "menu_id")
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数
     */
    private String queryParam;

    /**
     * 是否为外链
     */
    private String isExternalLink;

    /**
     * 是否缓存
     */
    private String isCache;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 显示设置
     */
    private String visible;

    /**
     * 启用状态
     */
    private String status;

    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;

    /**
     * 父菜单名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();

    /**
     * 获取路由名称
     */
    public String getRouteName() {
        String routerName = StringUtils.capitalize(path);
        // 内链菜单框架不设置路由名称
        if (isMenuFrame()) {
            routerName = StringUtils.EMPTY;
        }
        return routerName;
    }

    /**
     * 获取路由地址
     */
    public String getRouterPath() {
        String routerPath = this.path;

        // 真正的外链直接返回完整URL（会在新窗口打开）
        if (isExternalLink()) {
            return routerPath;
        }

        // 内链：在应用内打开外部URL（通过iframe等方式）
        if (getParentId() != 0L && isInnerLink()) {
            routerPath = innerLinkReplaceEach(routerPath);
        }

        // 非外链并且是一级目录（类型为目录）
        if (0L == getParentId() && SystemConstants.TYPE_DIR.equals(getMenuType())
            && !isExternalLink()) {
            routerPath = "/" + this.path;
        }
        // 内链菜单框架
        else if (isMenuFrame()) {
            routerPath = "/";
        }

        return routerPath;
    }

    /**
     * 获取组件信息
     */
    public String getComponentInfo() {
        String component = SystemConstants.LAYOUT;

        if (StringUtils.isNotEmpty(this.component) && !isMenuFrame()) {
            component = this.component;
        } else if (StringUtils.isEmpty(this.component) && getParentId() != 0L && isInnerLink()) {
            component = SystemConstants.INNER_LINK;
        } else if (StringUtils.isEmpty(this.component) && isParentView()) {
            component = SystemConstants.PARENT_VIEW;
        }

        return component;
    }

    /**
     * 是否为真正的外链（在新窗口打开）
     * 当 isExternalLink = '1' 且 path 是有效URL时，认为是外链
     */
    public boolean isExternalLink() {
        return DictBooleanFlag.YES.getValue().equals(isExternalLink) && Validator.isUrl(path);
    }

    /**
     * 是否为内链组件（在应用内通过iframe等方式打开外部URL）
     * 当 isExternalLink = '0' 但 path 是有效URL时，认为是内链
     */
    public boolean isInnerLink() {
        return DictBooleanFlag.NO.getValue().equals(isExternalLink) && Validator.isUrl(path);
    }

    /**
     * 是否为菜单内部跳转框架
     * 顶级菜单 + 菜单类型 + 内链 = 菜单框架
     */
    public boolean isMenuFrame() {
        return getParentId() == 0L && SystemConstants.TYPE_MENU.equals(menuType) && isInnerLink();
    }

    /**
     * 是否为parent_view组件
     */
    public boolean isParentView() {
        return getParentId() != 0L && SystemConstants.TYPE_DIR.equals(menuType);
    }

    /**
     * 内链域名特殊字符替换
     * 将URL中的特殊字符替换为路径分隔符，用于内链路由处理
     */
    public static String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[]{Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
            new String[]{"", "", "", "/", "/"});
    }
}
