package plus.ruoyi.common.openapi.annotation;

import java.lang.annotation.*;

/**
 * 开放API接口标识注解
 * 标识该接口可以通过OpenAPI方式访问(AppKey+AppSecret认证)
 *
 * @author 抓蛙师
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface OpenApi {

    /**
     * 接口说明
     */
    String value() default "";

}
