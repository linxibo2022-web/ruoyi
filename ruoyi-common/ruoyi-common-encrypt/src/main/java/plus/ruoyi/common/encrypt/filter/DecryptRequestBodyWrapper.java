package plus.ruoyi.common.encrypt.filter;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 解密请求体包装器
 * <p>
 * 继承HttpServletRequestWrapper，实现请求体的自动解密功能
 * <p>
 * 解密流程：
 * 1. 从请求头获取RSA加密的AES密钥
 * 2. 用RSA私钥解密获得AES密钥
 * 3. 对AES密钥进行Base64解码
 * 4. 用AES密钥解密请求体内容
 * 5. 重写getInputStream等方法返回解密后的内容
 * <p>
 * 适用场景：
 * - 前端需要传输敏感数据时
 * - API接口安全要求较高的场景
 * - 移动端与服务端通信加密
 *
 * @author wdhcr
 */
public class DecryptRequestBodyWrapper extends HttpServletRequestWrapper {

    /**
     * 解密后的请求体内容
     */
    private final byte[] body;

    /**
     * 构造函数，执行请求体解密
     *
     * @param request    原始请求对象
     * @param privateKey RSA私钥，用于解密AES密钥
     * @param headerFlag 请求头中加密密钥的标识
     * @throws IOException 读取请求体失败时抛出
     */
    public DecryptRequestBodyWrapper(HttpServletRequest request, String privateKey, String headerFlag)
        throws IOException {
        super(request);

        // 1. 从请求头获取RSA加密的AES密钥
        String headerRsa = request.getHeader(headerFlag);

        // 2. 用RSA私钥解密获得Base64编码的AES密钥
        String decryptAes = EncryptUtils.decryptByRsa(headerRsa, privateKey);

        // 3. 对AES密钥进行Base64解码
        String aesPassword = EncryptUtils.decryptByBase64(decryptAes);

        // 4. 读取原始请求体
        request.setCharacterEncoding(Constants.UTF8);
        byte[] readBytes = IoUtil.readBytes(request.getInputStream(), false);
        String requestBody = new String(readBytes, StandardCharsets.UTF_8);

        // 5. 用AES密钥解密请求体
        String decryptBody = EncryptUtils.decryptByAes(requestBody, aesPassword);
        body = decryptBody.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 重写getReader方法，返回解密后内容的Reader
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 重写getContentLength方法，返回解密后内容的长度
     */
    @Override
    public int getContentLength() {
        return body.length;
    }

    /**
     * 重写getContentLengthLong方法，返回解密后内容的长度
     */
    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    /**
     * 重写getContentType方法，固定返回JSON类型
     */
    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    /**
     * 重写getInputStream方法，返回解密后内容的输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public int available() {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 空实现
            }
        };
    }
}
