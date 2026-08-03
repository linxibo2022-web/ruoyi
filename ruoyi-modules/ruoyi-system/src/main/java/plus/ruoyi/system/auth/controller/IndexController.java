package plus.ruoyi.system.auth.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 首页控制器
 * 提供系统首页和基础信息展示
 *
 * @author 抓蛙师
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
public class IndexController {

    private final AppProperties appProperties;

    @Autowired(required = false)
    private SpringDocConfigProperties springDocConfigProperties;

    /**
     * 访问首页
     * 从模板文件加载HTML内容并替换占位符
     */
    @GetMapping(value = "/")
    public void index(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 从 resources/static 目录加载模板文件
            String htmlTemplate = loadHtmlTemplate("static/index.html");

            // 构建API文档信息
            String apiDocsSection = buildApiDocsSection();

            // 替换模板中的占位符
            String html = htmlTemplate
                .replace("${appTitle}", appProperties.getTitle())
                .replace("${appName}", SpringUtils.getApplicationName())
                .replace("${activeProfiles}", StringUtils.join(SpringUtils.getActiveProfiles(), ", "))
                .replace("${serverPort}", SpringUtils.getProperty("server.port"))
                .replace("${appVersion}", appProperties.getVersion())
                .replace("${apiDocsSection}", apiDocsSection);

            out.print(html);
        } catch (Exception ignored) {
            String fallbackHtml = StringUtils.format(
                "{} - 系统运行正常 | 应用:{} | 环境:{} | 端口:{} | 版本:{}",
                appProperties.getTitle(),
                SpringUtils.getApplicationName(),
                StringUtils.join(SpringUtils.getActiveProfiles(), ", "),
                SpringUtils.getProperty("server.port"),
                appProperties.getVersion()
            );
            out.print(fallbackHtml);
        } finally {
            out.flush();
        }
    }

    /**
     * 构建API文档区域HTML
     *
     * @return API文档区域的HTML字符串
     */
    private String buildApiDocsSection() {
        // 检查SpringDoc是否启用
        if (springDocConfigProperties == null || !springDocConfigProperties.getApiDocs().isEnabled()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"api-docs-section\">")
          .append("<h3 class=\"api-docs-title\">📚 API接口文档</h3>")
          .append("<div class=\"api-docs-grid\">");

        // 获取基础路径
        String basePath = springDocConfigProperties.getApiDocs().getPath();

        // 构建API文档链接列表
        List<ApiDocInfo> apiDocs = new ArrayList<>();

        // 添加完整文档
        apiDocs.add(new ApiDocInfo("Complete", "", basePath));

        // 添加分组文档
        Set<SpringDocConfigProperties.GroupConfig> groupConfigs = springDocConfigProperties.getGroupConfigs();
        if (groupConfigs != null && !groupConfigs.isEmpty()) {
            for (SpringDocConfigProperties.GroupConfig groupConfig : groupConfigs) {
                String groupName = groupConfig.getGroup();
                String displayName = groupConfig.getDisplayName() != null ?
                    groupConfig.getDisplayName() : StringUtils.capitalize(groupName);
                String groupUrl = basePath + "/" + groupName;

                apiDocs.add(new ApiDocInfo(displayName, "", groupUrl));
            }
        }

        // 生成HTML
        for (ApiDocInfo apiDoc : apiDocs) {
            sb.append(String.format(
                "<div class=\"api-doc-item\" onclick=\"window.open('%s', '_blank')\">" +
                "<div class=\"api-doc-name\">%s</div>" +
                "</div>",
                apiDoc.url(),
                apiDoc.name()
            ));
        }

        sb.append("</div></div>");
        return sb.toString();
    }

    /**
     * 从 classpath 加载 HTML 模板文件
     *
     * @param templatePath 模板文件路径
     * @return HTML 模板内容
     * @throws IOException 文件读取异常
     */
    private String loadHtmlTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (Scanner scanner = new Scanner(resource.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * 处理favicon.ico请求
     * 浏览器会自动请求网站的favicon图标,此方法用于处理该请求
     * 返回空响应,避免产生404错误
     */
    @GetMapping("/favicon.ico")
    public void favicon() {
        // 返回空响应
    }

    /**
     * API文档信息内部类
     */
    private record ApiDocInfo(String name, String description, String url) {
    }
}
