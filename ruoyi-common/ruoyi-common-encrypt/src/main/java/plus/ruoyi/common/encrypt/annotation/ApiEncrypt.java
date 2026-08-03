package plus.ruoyi.common.encrypt.annotation;

import java.lang.annotation.*;

/**
 * API接口加密注解
 *
 * 标注在Controller方法上，控制该接口的加密行为
 *
 * 使用示例：
 * <pre>
 * @PostMapping("/login")
 * @ApiEncrypt(response = true)  // 对响应进行加密
 * public Result<User> login(@RequestBody LoginRequest request) {
 *     // 请求自动解密，响应自动加密
 *     return Result.success(userService.login(request));
 * }
 * </pre>
 *
 * @author Michelle.Chung
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {
    /**
     * 是否对响应进行加密
     *
     * true: 对响应结果进行AES+RSA混合加密
     * false: 不加密响应（默认）
     *
     * 加密流程：
     * 1. 生成32位随机AES密钥
     * 2. 使用AES密钥加密响应数据
     * 3. 使用RSA公钥加密AES密钥
     * 4. 将加密后的AES密钥放入响应头
     *
     * @return 是否加密响应
     */
    boolean response() default false;
}
