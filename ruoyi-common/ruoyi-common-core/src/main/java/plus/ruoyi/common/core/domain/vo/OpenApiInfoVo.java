package plus.ruoyi.common.core.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 开放接口信息VO
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiInfoVo implements Serializable {

    /**
     * 接口路径
     */
    private String path;

    /**
     * 请求方法 (GET/POST/PUT/DELETE)
     */
    private String method;

    /**
     * 接口说明
     */
    private String description;

    /**
     * 所属模块
     */
    private String module;

    /**
     * Controller类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 权限要求
     */
    private String permission;

    /**
     * 权限模式 (AND/OR)
     */
    private String permissionMode;

    /**
     * 角色要求
     */
    private String roleCode;

    /**
     * 角色模式 (AND/OR)
     */
    private String roleMode;

    /**
     * 是否无权限限制
     */
    private Boolean noAuth;

    /**
     * 请求参数列表
     */
    private List<ParameterInfo> parameters;

    /**
     * 响应类型
     */
    private String responseType;

    /**
     * 响应信息(包含泛型和字段详情)
     */
    private ResponseInfo responseInfo;

    /**
     * 响应信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseInfo implements Serializable {

        /**
         * 响应完整类型(包含泛型)
         * 例如: R<PageResult<AdVo>>, List<UserVo>, String
         */
        private String fullType;

        /**
         * 响应体的实际类型(泛型参数)
         * 例如: PageResult<AdVo>, List<AdVo>, AdVo
         */
        private String dataType;

        /**
         * 响应体字段信息(仅当dataType是对象类型时有值)
         */
        private List<FieldInfo> fields;

        /**
         * 响应示例JSON
         */
        private String example;
    }

    /**
     * 参数信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterInfo implements Serializable {

        /**
         * 参数名
         */
        private String name;

        /**
         * 参数类型
         */
        private String type;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 参数说明
         */
        private String description;

        /**
         * 参数位置 (PATH/QUERY/BODY/FORM/HEADER)
         * @see ParameterLocation
         */
        private String location;

        /**
         * 子字段列表(用于复杂对象类型)
         */
        private List<FieldInfo> fields;
    }

    /**
     * 字段信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldInfo implements Serializable {

        /**
         * 字段名
         */
        private String name;

        /**
         * 字段类型
         */
        private String type;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 字段说明
         */
        private String description;

        /**
         * 示例值
         */
        private String example;
    }

    /**
     * 参数位置常量
     */
    public static class ParameterLocation {
        /** 路径参数 */
        public static final String PATH = "PATH";
        /** 查询参数 */
        public static final String QUERY = "QUERY";
        /** 请求体 */
        public static final String BODY = "BODY";
        /** 表单参数 */
        public static final String FORM = "FORM";
        /** 请求头 */
        public static final String HEADER = "HEADER";
        /** 默认(未指定) */
        public static final String UNDEFINED = "UNDEFINED";
    }
}
