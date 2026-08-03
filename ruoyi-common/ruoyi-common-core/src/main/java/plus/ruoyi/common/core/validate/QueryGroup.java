package plus.ruoyi.common.core.validate;


/**
 * 查询操作校验分组
 * <p>用于标识查询操作时需要进行的字段校验，对应service.query()方法</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @Size(min = 1, max = 50, groups = QueryGroup.class, message = "用户名长度必须在1-50之间")
 * private String userName;
 *
 * @Valid
 * @NotNull(groups = QueryGroup.class)
 * private PageQuery pageQuery;
 * }</pre>
 *
 * @author Lion Li
 */
public interface QueryGroup {
}
