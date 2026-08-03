package plus.ruoyi.common.doctemplate.builder;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.doctemplate.exception.DocTemplateException;
import plus.ruoyi.common.test.config.TestConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocTemplateBuilder 单元测试
 *
 * @author 抓蛙师
 */
class DocTemplateBuilderTest {

    private String tempDir;
    private byte[] simpleTemplateBytes;
    private byte[] tableTemplateBytes;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = TestConfig.createTestSubDir("doctemplate");
        System.out.println("============================================");
        System.out.println("测试输出目录: " + tempDir);
        System.out.println("============================================");
        simpleTemplateBytes = createSimpleTemplate();
        tableTemplateBytes = createTableTemplate();
    }

    /**
     * 创建简单的文本模板
     * 模板内容：Hello {{name}}! Today is {{date}}.
     */
    private byte[] createSimpleTemplate() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText("Hello {{name}}! Today is {{date}}.");
            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 创建包含表格循环的模板
     * <p>
     * POI-TL LoopRowTableRenderPolicy 格式要求（3行结构）：
     * <pre>
     * | 表头行    |        |          |
     * | {{items}} |        |          |  ← 标记行（渲染后删除）
     * | [no]      | [name] | [amount] |  ← 模板行（循环复制）
     * </pre>
     */
    private byte[] createTableTemplate() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 添加标题
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText("Report: {{title}}");

            // 创建表格（3行：表头 + 标记行 + 模板行）
            XWPFTable table = doc.createTable(3, 3);

            // 第1行：表头
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("序号");
            headerRow.getCell(1).setText("名称");
            headerRow.getCell(2).setText("金额");

            // 第2行：标记行（只需第一列放 {{items}}，渲染后此行会被删除）
            XWPFTableRow tagRow = table.getRow(1);
            tagRow.getCell(0).setText("{{items}}");
            // 其他单元格留空

            // 第3行：模板行（使用 [字段名] 引用数据，此行会被循环复制）
            XWPFTableRow templateRow = table.getRow(2);
            templateRow.getCell(0).setText("[no]");
            templateRow.getCell(1).setText("[name]");
            templateRow.getCell(2).setText("[amount]");

            doc.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void testBasicTextReplacement() {
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "张三")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证是 DOCX 格式（ZIP 文件头）
        assertEquals('P', result[0]);
        assertEquals('K', result[1]);
    }

    @Test
    void testBatchData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "李四");
        data.put("date", "2025-12-25");

        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data(data)
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testFromByteArray() {
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "测试")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testFromInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simpleTemplateBytes);

        byte[] result = DocTemplateBuilder.of(inputStream)
            .data("name", "测试")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testFromFile() throws IOException {
        // 先保存模板到文件
        String templatePath = tempDir + File.separator + "template.docx";
        try (FileOutputStream fos = new FileOutputStream(templatePath)) {
            fos.write(simpleTemplateBytes);
        }

        byte[] result = DocTemplateBuilder.of(new File(templatePath))
            .data("name", "文件测试")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testFromFilePath() throws IOException {
        // 先保存模板到文件
        String templatePath = tempDir + File.separator + "template2.docx";
        try (FileOutputStream fos = new FileOutputStream(templatePath)) {
            fos.write(simpleTemplateBytes);
        }

        byte[] result = DocTemplateBuilder.of(templatePath)
            .data("name", "路径测试")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildToFile() {
        String outputPath = tempDir + File.separator + "output.docx";

        assertDoesNotThrow(() -> {
            DocTemplateBuilder.of(simpleTemplateBytes)
                .data("name", "输出测试")
                .data("date", "2025-01-01")
                .buildToFile(outputPath);
        });

        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    void testBuildToStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "流输出测试")
            .data("date", "2025-01-01")
            .buildToStream(outputStream);

        byte[] result = outputStream.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testChainedConfiguration() {
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "链式调用")
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testNullTemplateStream() {
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.of((java.io.InputStream) null);
        });
    }

    @Test
    void testEmptyTemplateBytes() {
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.of(new byte[0]);
        });
    }

    @Test
    void testNonExistentFile() {
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.of(new File("/non/existent/path/template.docx"));
        });
    }

    @Test
    void testNullOutputStream() {
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.of(simpleTemplateBytes)
                .data("name", "测试")
                .buildToStream(null);
        });
    }

    @Test
    void testNullData() {
        // null key 应该被忽略
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data(null, "value")
            .data("name", null)
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testEmptyData() {
        // 空数据应该正常处理
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("", "value")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testObjectData() {
        // 测试对象属性访问
        TestUser user = new TestUser("王五", 25, "test@example.com");

        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", user.getName())
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testTableData() {
        List<Map<String, Object>> items = Arrays.asList(
            createItem(1, "商品A", 100.00),
            createItem(2, "商品B", 200.00),
            createItem(3, "商品C", 300.00)
        );

        // 注意：表格循环需要正确配置模板
        byte[] result = DocTemplateBuilder.of(tableTemplateBytes)
            .data("title", "销售报表")
            .table("items", items)
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSpecialCharacters() {
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "特殊字符 <>&\"'!@#$%^&*()")
            .data("date", "2025/01/01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testChineseCharacters() {
        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", "中文测试张三李四王五")
            .data("date", "二零二五年一月一日")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testLongText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("这是一段很长的测试文本。");
        }

        byte[] result = DocTemplateBuilder.of(simpleTemplateBytes)
            .data("name", longText.toString())
            .data("date", "2025-01-01")
            .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * 测试完整流程：从 URL 加载模板 → 替换占位符 → 输出到文件
     * <p>
     * 注意：此测试需要先在本地创建一个模板文件模拟在线模板，
     * 实际使用时可替换为真实的在线模板 URL
     */
    @Test
    void testCompleteFlowFromUrlToFile() throws IOException {
        // 1. 准备：先将模板保存到本地文件（模拟在线模板）
        String templatePath = tempDir + File.separator + "online_template.docx";
        try (FileOutputStream fos = new FileOutputStream(templatePath)) {
            fos.write(simpleTemplateBytes);
        }

        // 2. 使用 file:// 协议模拟 URL 加载（实际使用时替换为 http/https URL）
        File templateFile = new File(templatePath);
        String fileUrl = templateFile.toURI().toURL().toString();

        // 3. 定义输出文件路径
        String outputPath = tempDir + File.separator + "output_from_url.docx";

        // 4. 完整流程：URL 加载 → 数据替换 → 文件输出
        assertDoesNotThrow(() -> {
            DocTemplateBuilder.ofUrl(fileUrl)
                .data("name", "在线模板测试")
                .data("date", "2025-12-29")
                .buildToFile(outputPath);
        });

        // 5. 验证输出文件
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "输出文件应该存在");
        assertTrue(outputFile.length() > 0, "输出文件不应为空");

        // 6. 验证文件格式正确（DOCX 是 ZIP 格式）
        try (java.io.FileInputStream fis = new java.io.FileInputStream(outputFile)) {
            byte[] header = new byte[2];
            fis.read(header);
            assertEquals('P', header[0], "应该是 ZIP 格式文件头");
            assertEquals('K', header[1], "应该是 ZIP 格式文件头");
        }
    }

    /**
     * 测试从 URL 加载表格模板的完整流程
     */
    @Test
    void testCompleteFlowWithTableFromUrl() throws IOException {
        // 1. 准备表格模板
        String templatePath = tempDir + File.separator + "table_template_online.docx";
        try (FileOutputStream fos = new FileOutputStream(templatePath)) {
            fos.write(tableTemplateBytes);
        }

        // 2. 模拟 URL
        File templateFile = new File(templatePath);
        String fileUrl = templateFile.toURI().toURL().toString();

        // 3. 准备表格数据
        List<Map<String, Object>> items = Arrays.asList(
            createItem(1, "苹果", 15.50),
            createItem(2, "香蕉", 8.80),
            createItem(3, "橙子", 12.00),
            createItem(4, "葡萄", 25.00)
        );

        // 4. 输出路径
        String outputPath = tempDir + File.separator + "report_from_url.docx";

        // 5. 完整流程
        assertDoesNotThrow(() -> {
            DocTemplateBuilder.ofUrl(fileUrl)
                .data("title", "水果销售月报")
                .table("items", items)
                .buildToFile(outputPath);
        });

        // 6. 验证
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * 测试图片占位符替换（指定合理尺寸）
     * <p>
     * 模板中使用 {@code {{@logo}}} 作为图片占位符
     */
    @Test
    void testImageReplacementFromUrl() throws IOException {
        // 1. 创建带图片占位符的模板
        byte[] imageTemplateBytes = createImageTemplate();

        // 2. 输出原始模板文件（方便查看占位符效果）
        String templateOutputPath = tempDir + File.separator + "image_template_original.docx";
        try (FileOutputStream fos = new FileOutputStream(templateOutputPath)) {
            fos.write(imageTemplateBytes);
        }
        System.out.println("原始模板已输出到: " + templateOutputPath);

        // 3. 准备输出路径
        String outputPath = tempDir + File.separator + "output_with_image.docx";

        // 4. 使用在线图片 URL 替换占位符，指定合理尺寸（80x80像素）
        assertDoesNotThrow(() -> {
            DocTemplateBuilder.of(imageTemplateBytes)
                .data("title", "RuoYi-Plus 项目介绍")
                .data("description", "一款优秀的企业级快速开发框架")
                .image("logo", "https://ruoyi.plus/logo.png", 80, 80)  // 80x80 像素
                .buildToFile(outputPath);
        });
        System.out.println("替换后文档已输出到: " + outputPath);

        // 5. 验证输出文件
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "输出文件应该存在");
        assertTrue(outputFile.length() > 0, "输出文件不应为空");

        // 6. 验证文件格式
        try (java.io.FileInputStream fis = new java.io.FileInputStream(outputFile)) {
            byte[] header = new byte[2];
            fis.read(header);
            assertEquals('P', header[0]);
            assertEquals('K', header[1]);
        }
    }

    /**
     * 测试图片居中显示
     */
    @Test
    void testImageCentered() throws IOException {
        // 1. 创建带图片占位符的模板
        byte[] imageTemplateBytes = createImageTemplate();

        // 2. 准备输出路径
        String outputPath = tempDir + File.separator + "output_with_centered_image.docx";

        // 3. 使用居中显示的图片（60x60 像素）
        assertDoesNotThrow(() -> {
            DocTemplateBuilder.of(imageTemplateBytes)
                .data("title", "RuoYi-Plus 框架")
                .data("description", "图片居中显示测试")
                .imageCenter("logo", "https://ruoyi.plus/logo.png", 60, 60)  // 居中 60x60
                .buildToFile(outputPath);
        });

        // 4. 验证
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * 测试图片适应模板占位符大小（fitSize）
     */
    @Test
    void testImageFitSize() throws IOException {
        // 1. 创建带图片占位符的模板
        byte[] imageTemplateBytes = createImageTemplate();

        // 2. 准备输出路径
        String outputPath = tempDir + File.separator + "output_with_fitsize_image.docx";

        // 3. 使用 fitSize 让图片适应模板占位符大小
        assertDoesNotThrow(() -> {
            DocTemplateBuilder.of(imageTemplateBytes)
                .data("title", "RuoYi-Plus 框架")
                .data("description", "图片适应占位符大小测试")
                .imageFitSize("logo", "https://ruoyi.plus/logo.png")  // 适应模板大小
                .buildToFile(outputPath);
        });

        // 4. 验证
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * 创建带图片占位符的模板
     * 模板内容：标题 + 图片 + 描述
     */
    private byte[] createImageTemplate() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 标题
            XWPFParagraph titlePara = doc.createParagraph();
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("{{title}}");
            titleRun.setBold(true);

            // 图片占位符（POI-TL 使用 {{@变量名}} 表示图片）
            XWPFParagraph imgPara = doc.createParagraph();
            XWPFRun imgRun = imgPara.createRun();
            imgRun.setText("{{@logo}}");

            // 描述
            XWPFParagraph descPara = doc.createParagraph();
            XWPFRun descRun = descPara.createRun();
            descRun.setText("{{description}}");

            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 测试 URL 加载失败的情况
     */
    @Test
    void testUrlLoadingFailure() {
        // 测试无效 URL
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.ofUrl("http://invalid.nonexistent.domain/template.docx");
        });

        // 测试空 URL
        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.ofUrl((String) null);
        });

        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.ofUrl("");
        });

        assertThrows(DocTemplateException.class, () -> {
            DocTemplateBuilder.ofUrl((java.net.URL) null);
        });
    }

    private Map<String, Object> createItem(int no, String name, double amount) {
        Map<String, Object> item = new HashMap<>();
        item.put("no", no);
        item.put("name", name);
        item.put("amount", amount);
        return item;
    }

    /**
     * 测试用户类
     */
    static class TestUser {
        private String name;
        private int age;
        private String email;

        public TestUser(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getEmail() {
            return email;
        }
    }
}
