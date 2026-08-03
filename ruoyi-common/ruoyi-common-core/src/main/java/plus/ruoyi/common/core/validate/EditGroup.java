package plus.ruoyi.common.core.validate;


/**
 * 编辑操作校验分组
 * <p>用于标识编辑操作时需要进行的字段校验，对应Controller的edit/update方法和Service的edit/update方法</p>
 * <p>常见使用场景：</p>
 * <ul>
 *     <li>编辑时ID字段不能为空（使用@NotNull校验）</li>
 *     <li>编辑时可选填字段的格式校验</li>
 *     <li>编辑时需要进行数据存在性校验的字段</li>
 *     <li>编辑时的业务规则校验</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * public class UserBo {
 *     @Null(groups = AddGroup.class, message = "新增时ID必须为空")
 *     @NotNull(groups = EditGroup.class, message = "编辑时ID不能为空")
 *     @Min(value = 1, groups = EditGroup.class, message = "ID必须大于0")
 *     private Long id;
 *
 *     @NotBlank(groups = {AddGroup.class, EditGroup.class}, message = "用户名不能为空")
 *     private String userName;
 *
 *     @Email(groups = EditGroup.class, message = "邮箱格式不正确")
 *     private String email;
 * }
 *
 * // Controller中的使用
 * @PutMapping("/update")
 * public R<Void> update(@Validated(EditGroup.class) @RequestBody UserBo bo) {
 *     return R.status(userService.update(bo));
 * }
 * }</pre>
 *
 * @author Lion Li
 * @since 1.0
 */
public interface EditGroup {
}
