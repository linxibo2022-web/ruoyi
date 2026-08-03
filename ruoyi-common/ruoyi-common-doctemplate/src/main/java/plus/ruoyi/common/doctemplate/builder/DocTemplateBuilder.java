package plus.ruoyi.common.doctemplate.builder;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.Pictures;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.doctemplate.exception.DocTemplateException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word 文档模板构建器
 * <p>
 * 基于 POI-TL 实现 Word 文档的占位符替换功能，支持：
 * <ul>
 *     <li>文本替换：{@code {{name}}}</li>
 *     <li>对象属性：{@code {{obj.field}}}</li>
 *     <li>图片插入：{@code {{@imgKey}}}</li>
 *     <li>表格行循环：{@code {{tableKey}}[field]} + {@code [field]}</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 从文件加载模板
 * byte[] result = DocTemplateBuilder.of(new File("template.docx"))
 *     .data("title", "报告标题")
 *     .data("user", userVo)  // 对象属性通过 {{user.name}} 访问
 *     .image("sign", signatureBytes)
 *     .table("items", itemList)  // 表格循环
 *     .build();
 *
 * // 从 InputStream 加载模板
 * byte[] result = DocTemplateBuilder.of(inputStream)
 *     .data("name", "张三")
 *     .build();
 * }</pre>
 *
 * @author 抓蛙师
 */
@Slf4j
public class DocTemplateBuilder {

    /**
     * 模板输入流
     */
    private final InputStream templateStream;

    /**
     * 数据映射
     */
    private final Map<String, Object> dataMap;

    /**
     * 表格循环字段名列表（用于配置 LoopRowTableRenderPolicy）
     */
    private final Map<String, Boolean> tableFields;

    /**
     * 私有构造函数
     *
     * @param templateStream 模板输入流
     */
    private DocTemplateBuilder(InputStream templateStream) {
        this.templateStream = templateStream;
        this.dataMap = new HashMap<>();
        this.tableFields = new HashMap<>();
    }

    /**
     * 从 InputStream 创建构建器
     *
     * @param templateStream 模板输入流
     * @return 构建器实例
     */
    public static DocTemplateBuilder of(InputStream templateStream) {
        if (templateStream == null) {
            throw new DocTemplateException("模板输入流不能为空");
        }
        return new DocTemplateBuilder(templateStream);
    }

    /**
     * 从字节数组创建构建器
     *
     * @param templateBytes 模板字节数组
     * @return 构建器实例
     */
    public static DocTemplateBuilder of(byte[] templateBytes) {
        if (templateBytes == null || templateBytes.length == 0) {
            throw new DocTemplateException("模板字节数组不能为空");
        }
        return new DocTemplateBuilder(new ByteArrayInputStream(templateBytes));
    }

    /**
     * 从文件创建构建器
     *
     * @param templateFile 模板文件
     * @return 构建器实例
     */
    public static DocTemplateBuilder of(File templateFile) {
        if (templateFile == null || !templateFile.exists()) {
            throw new DocTemplateException("模板文件不存在: " + (templateFile != null ? templateFile.getPath() : "null"));
        }
        try {
            return new DocTemplateBuilder(new FileInputStream(templateFile));
        } catch (IOException e) {
            throw new DocTemplateException("读取模板文件失败: " + templateFile.getPath(), e);
        }
    }

    /**
     * 从文件路径创建构建器
     *
     * @param templatePath 模板文件路径
     * @return 构建器实例
     */
    public static DocTemplateBuilder of(String templatePath) {
        return of(new File(templatePath));
    }

    /**
     * 从 URL 创建构建器
     * <p>
     * 支持从网络地址加载模板文件
     *
     * @param templateUrl 模板 URL 地址
     * @return 构建器实例
     */
    public static DocTemplateBuilder ofUrl(String templateUrl) {
        if (templateUrl == null || templateUrl.isEmpty()) {
            throw new DocTemplateException("模板 URL 不能为空");
        }
        try {
            URL url = URI.create(templateUrl).toURL();
            return new DocTemplateBuilder(url.openStream());
        } catch (IOException e) {
            throw new DocTemplateException("从 URL 加载模板失败: " + templateUrl, e);
        }
    }

    /**
     * 从 URL 对象创建构建器
     *
     * @param templateUrl 模板 URL 对象
     * @return 构建器实例
     */
    public static DocTemplateBuilder ofUrl(URL templateUrl) {
        if (templateUrl == null) {
            throw new DocTemplateException("模板 URL 不能为空");
        }
        try {
            return new DocTemplateBuilder(templateUrl.openStream());
        } catch (IOException e) {
            throw new DocTemplateException("从 URL 加载模板失败: " + templateUrl, e);
        }
    }

    /**
     * 添加单个数据
     * <p>
     * 模板中使用 {@code {{key}}} 引用，如果 value 是对象，可使用 {@code {{key.field}}} 访问属性
     *
     * @param key   占位符名称
     * @param value 替换值（支持 String、Number、对象等）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder data(String key, Object value) {
        if (key != null && !key.isEmpty()) {
            dataMap.put(key, value);
        }
        return this;
    }

    /**
     * 批量添加数据
     *
     * @param data 数据映射
     * @return 当前构建器实例
     */
    public DocTemplateBuilder data(Map<String, Object> data) {
        if (data != null) {
            dataMap.putAll(data);
        }
        return this;
    }

    /**
     * 添加图片（从字节数组）
     * <p>
     * 模板中使用 {@code {{@key}}} 引用图片占位符
     *
     * @param key        图片占位符名称（不含 @）
     * @param imageBytes 图片字节数组
     * @return 当前构建器实例
     */
    public DocTemplateBuilder image(String key, byte[] imageBytes) {
        if (key != null && !key.isEmpty() && imageBytes != null && imageBytes.length > 0) {
            dataMap.put(key, Pictures.ofBytes(imageBytes).create());
        }
        return this;
    }

    /**
     * 添加图片（从输入流）
     * <p>
     * 模板中使用 {@code {{@key}}} 引用图片占位符
     *
     * @param key         图片占位符名称（不含 @）
     * @param imageStream 图片输入流
     * @return 当前构建器实例
     */
    public DocTemplateBuilder image(String key, InputStream imageStream) {
        if (key != null && !key.isEmpty() && imageStream != null) {
            dataMap.put(key, Pictures.ofStream(imageStream).create());
        }
        return this;
    }

    /**
     * 添加图片（从 URL）
     * <p>
     * 模板中使用 {@code {{@key}}} 引用图片占位符
     *
     * @param key      图片占位符名称（不含 @）
     * @param imageUrl 图片 URL 地址
     * @return 当前构建器实例
     */
    public DocTemplateBuilder image(String key, String imageUrl) {
        if (key != null && !key.isEmpty() && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                dataMap.put(key, Pictures.ofUrl(imageUrl).create());
            } catch (Exception e) {
                log.warn("加载图片失败: {}, URL: {}", e.getMessage(), imageUrl);
            }
        }
        return this;
    }

    /**
     * 添加图片（从 URL，指定尺寸）
     * <p>
     * 模板中使用 {@code {{@key}}} 引用图片占位符
     *
     * @param key      图片占位符名称（不含 @）
     * @param imageUrl 图片 URL 地址
     * @param width    图片宽度（像素）
     * @param height   图片高度（像素）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder image(String key, String imageUrl, int width, int height) {
        if (key != null && !key.isEmpty() && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                dataMap.put(key, Pictures.ofUrl(imageUrl).size(width, height).create());
            } catch (Exception e) {
                log.warn("加载图片失败: {}, URL: {}", e.getMessage(), imageUrl);
            }
        }
        return this;
    }

    /**
     * 添加图片（从字节数组，指定尺寸）
     * <p>
     * 模板中使用 {@code {{@key}}} 引用图片占位符
     *
     * @param key        图片占位符名称（不含 @）
     * @param imageBytes 图片字节数组
     * @param width      图片宽度（像素）
     * @param height     图片高度（像素）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder image(String key, byte[] imageBytes, int width, int height) {
        if (key != null && !key.isEmpty() && imageBytes != null && imageBytes.length > 0) {
            dataMap.put(key, Pictures.ofBytes(imageBytes).size(width, height).create());
        }
        return this;
    }

    /**
     * 添加图片（适应模板占位符大小）- 推荐方式
     * <p>
     * 此方法会让图片自动适应模板中占位符图片的尺寸。
     * <b>使用方法</b>：在 Word 模板中插入一张占位图片，调整好大小和位置，
     * 然后在图片的"替代文字"中填写 {@code {{@key}}}，代码会用新图片替换并保持相同尺寸。
     *
     * @param key      图片占位符名称（不含 @）
     * @param imageUrl 图片 URL 地址
     * @return 当前构建器实例
     */
    public DocTemplateBuilder imageFitSize(String key, String imageUrl) {
        if (key != null && !key.isEmpty() && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                dataMap.put(key, Pictures.ofUrl(imageUrl).fitSize().create());
            } catch (Exception e) {
                log.warn("加载图片失败: {}, URL: {}", e.getMessage(), imageUrl);
            }
        }
        return this;
    }

    /**
     * 添加图片（适应模板占位符大小，从字节数组）
     *
     * @param key        图片占位符名称（不含 @）
     * @param imageBytes 图片字节数组
     * @return 当前构建器实例
     */
    public DocTemplateBuilder imageFitSize(String key, byte[] imageBytes) {
        if (key != null && !key.isEmpty() && imageBytes != null && imageBytes.length > 0) {
            dataMap.put(key, Pictures.ofBytes(imageBytes).fitSize().create());
        }
        return this;
    }

    /**
     * 添加图片（适应模板占位符大小，从输入流）
     *
     * @param key         图片占位符名称（不含 @）
     * @param imageStream 图片输入流
     * @return 当前构建器实例
     */
    public DocTemplateBuilder imageFitSize(String key, InputStream imageStream) {
        if (key != null && !key.isEmpty() && imageStream != null) {
            dataMap.put(key, Pictures.ofStream(imageStream).fitSize().create());
        }
        return this;
    }

    /**
     * 添加图片（居中显示，指定尺寸）
     * <p>
     * 图片会在段落中居中显示
     *
     * @param key      图片占位符名称（不含 @）
     * @param imageUrl 图片 URL 地址
     * @param width    图片宽度（像素）
     * @param height   图片高度（像素）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder imageCenter(String key, String imageUrl, int width, int height) {
        if (key != null && !key.isEmpty() && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                dataMap.put(key, Pictures.ofUrl(imageUrl).size(width, height).center().create());
            } catch (Exception e) {
                log.warn("加载图片失败: {}, URL: {}", e.getMessage(), imageUrl);
            }
        }
        return this;
    }

    /**
     * 添加图片（居中显示，从字节数组）
     *
     * @param key        图片占位符名称（不含 @）
     * @param imageBytes 图片字节数组
     * @param width      图片宽度（像素）
     * @param height     图片高度（像素）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder imageCenter(String key, byte[] imageBytes, int width, int height) {
        if (key != null && !key.isEmpty() && imageBytes != null && imageBytes.length > 0) {
            dataMap.put(key, Pictures.ofBytes(imageBytes).size(width, height).center().create());
        }
        return this;
    }

    /**
     * 添加表格行循环数据
     * <p>
     * 使用 {@code LoopRowTableRenderPolicy} 实现表格行循环。
     * <p>
     * <b>模板格式要求（3行结构）</b>：
     * <pre>
     * | 表头行    |        |          |
     * | {{items}} |        |          |  ← 标记行（渲染后删除）
     * | [no]      | [name] | [amount] |  ← 模板行（循环复制）
     * </pre>
     * <ul>
     *     <li>标记行：第一列放 {@code {{key}}}，其他列留空，渲染后此行被删除</li>
     *     <li>模板行：使用 {@code [字段名]} 格式引用数据属性，此行会被循环复制</li>
     * </ul>
     * <p>
     * 数据示例：
     * <pre>{@code
     * List<Map<String, Object>> items = List.of(
     *     Map.of("no", 1, "name", "苹果", "amount", 15.5),
     *     Map.of("no", 2, "name", "香蕉", "amount", 8.8)
     * );
     * builder.table("items", items);
     * }</pre>
     *
     * @param key  表格占位符名称
     * @param list 数据列表（支持 {@code List<Map>} 或 {@code List<POJO>}）
     * @return 当前构建器实例
     */
    public DocTemplateBuilder table(String key, List<?> list) {
        if (key != null && !key.isEmpty() && list != null) {
            dataMap.put(key, list);
            tableFields.put(key, true);
        }
        return this;
    }

    /**
     * 构建文档并返回字节数组
     *
     * @return 生成的 Word 文档字节数组
     * @throws DocTemplateException 构建失败时抛出异常
     */
    public byte[] build() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildToStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new DocTemplateException("构建文档失败", e);
        }
    }

    /**
     * 构建文档并写入输出流
     *
     * @param outputStream 目标输出流
     * @throws DocTemplateException 构建失败时抛出异常
     */
    public void buildToStream(OutputStream outputStream) {
        if (outputStream == null) {
            throw new DocTemplateException("输出流不能为空");
        }
        try {
            // 构建配置
            ConfigureBuilder configBuilder = Configure.builder();

            // 为表格循环字段添加 LoopRowTableRenderPolicy
            LoopRowTableRenderPolicy loopRowPolicy = new LoopRowTableRenderPolicy();
            for (String tableKey : tableFields.keySet()) {
                configBuilder.bind(tableKey, loopRowPolicy);
            }

            Configure config = configBuilder.build();

            // 编译模板并渲染数据
            XWPFTemplate template = XWPFTemplate.compile(templateStream, config);
            template.render(dataMap);
            template.writeAndClose(outputStream);
        } catch (Exception e) {
            throw new DocTemplateException("构建文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建文档并保存到文件
     *
     * @param outputFile 目标文件
     * @throws DocTemplateException 构建失败时抛出异常
     */
    public void buildToFile(File outputFile) {
        if (outputFile == null) {
            throw new DocTemplateException("输出文件不能为空");
        }
        try {
            // 确保父目录存在
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                buildToStream(fos);
            }
        } catch (IOException e) {
            throw new DocTemplateException("保存文档失败: " + outputFile.getPath(), e);
        }
    }

    /**
     * 构建文档并保存到文件路径
     *
     * @param outputPath 目标文件路径
     * @throws DocTemplateException 构建失败时抛出异常
     */
    public void buildToFile(String outputPath) {
        buildToFile(new File(outputPath));
    }
}
