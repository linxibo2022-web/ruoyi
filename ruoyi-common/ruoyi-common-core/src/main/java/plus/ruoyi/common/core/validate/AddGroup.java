package plus.ruoyi.common.core.validate;

/**
 * 新增操作校验分组
 * <p>用于标识新增操作时需要进行的字段校验，对应Controller的add方法和Service的add方法</p>
 * <p>常见使用场景：</p>
 * <ul>
 *     <li>新增时ID字段必须为空（使用@Null校验）</li>
 *     <li>新增时必填字段不能为空（使用@NotNull、@NotBlank校验）</li>
 *     <li>新增时需要进行唯一性校验的字段</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * public class UserBo {
 *     @Null(groups = AddGroup.class, message = "新增时ID必须为空")
 *     @NotNull(groups = EditGroup.class, message = "编辑时ID不能为空")
 *     private Long id;
 *
 *     @NotBlank(groups = AddGroup.class, message = "用户名不能为空")
 *     private String userName;
 *
 *     @Pattern(regexp = "^1[3-9]\\d{9}$", groups = AddGroup.class, message = "手机号格式不正确")
 *     private String phone;
 * }
 *
 * // Controller中的使用
 * @PostMapping("/add")
 * public R<Long> add(@Validated(AddGroup.class) @RequestBody UserBo bo) {
 *     return R.ok(userService.add(bo));
 * }
 * }</pre>
 *
 * @author Lion Li
 */
public interface AddGroup {
}
