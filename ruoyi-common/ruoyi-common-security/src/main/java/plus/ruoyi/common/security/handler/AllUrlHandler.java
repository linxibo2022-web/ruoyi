package plus.ruoyi.common.security.handler;

import cn.hutool.core.util.ReUtil;
import plus.ruoyi.common.core.utils.SpringUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 应用所有URL路径收集器
 * <p>
 * 自动收集Spring MVC中所有Controller的URL映射路径，用于安全拦截器的路径匹配
 * 主要功能：
 * 1. 在Spring容器初始化完成后自动扫描所有RequestMapping
 * 2. 提取所有URL路径模式并标准化处理
 * 3. 将路径变量占位符（如{id}）替换为通配符*以便于匹配
 * 4. 去除重复路径，提供唯一的URL集合
 * <p>
 * 应用场景：
 * - 安全拦截器需要知道哪些路径需要进行权限验证
 * - 动态权限控制，避免硬编码URL路径
 * - 与白名单配置配合，实现灵活的访问控制
 *
 * @author Lion Li
 */
@Data
public class AllUrlHandler implements InitializingBean {

    /**
     * 路径变量占位符匹配模式
     * 用于匹配Spring MVC路径中的变量占位符，如：{id}、{userId}、{name}等
     */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    /**
     * 存储所有收集到的URL路径
     * 路径格式示例：
     * - /api/user/* （原路径：/api/user/{id}）
     * - /system/role/list
     */
    private List<String> urls = new ArrayList<>();

    /**
     * Spring容器初始化完成后的回调方法
     * <p>
     * 执行流程：
     * 1. 获取RequestMappingHandlerMapping实例
     * 2. 遍历所有注册的HandlerMethod
     * 3. 提取URL路径模式并进行标准化处理
     * 4. 使用Set去重，避免重复路径
     * 5. 将结果存储到urls列表中
     */
    @Override
    public void afterPropertiesSet() {
        // 使用Set确保URL路径的唯一性
        Set<String> urlSet = new HashSet<>();

        // 获取Spring MVC的请求映射处理器
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        // 获取所有的请求映射信息和对应的处理方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        // 遍历所有请求映射信息
        handlerMethods.keySet().forEach(requestMappingInfo -> {
            // 获取路径模式条件中的所有路径
            Objects.requireNonNull(requestMappingInfo.getPathPatternsCondition().getPatterns())
                    .forEach(pathPattern -> {
                        // 获取路径字符串
                        String urlPattern = pathPattern.getPatternString();
                        // 将路径变量占位符（如{id}）替换为通配符*，便于后续匹配
                        String processedUrl = ReUtil.replaceAll(urlPattern, PATTERN, "*");
                        urlSet.add(processedUrl);
                    });
        });

        // 将去重后的URL路径添加到结果列表中
        urls.addAll(urlSet);
    }
}
