package plus.ruoyi.common.pay.domain.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 支付回调响应对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class NotifyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 返回状态码
     */
    private String returnCode;

    /**
     * 返回信息
     */
    private String returnMsg;

    /**
     * 创建成功响应
     */
    public static NotifyResponse success() {
        return NotifyResponse.builder()
            .returnCode("SUCCESS")
            .returnMsg("OK")
            .build();
    }

    /**
     * 创建成功响应(自定义消息)
     */
    public static NotifyResponse success(String message) {
        return NotifyResponse.builder()
            .returnCode("SUCCESS")
            .returnMsg(message)
            .build();
    }

    /**
     * 创建失败响应
     */
    public static NotifyResponse fail(String message) {
        return NotifyResponse.builder()
            .returnCode("FAIL")
            .returnMsg(message)
            .build();
    }

    /**
     * 转换为微信支付回调响应XML格式
     */
    public String toWxXml() {
        return String.format(
            "<xml><return_code><![CDATA[%s]]></return_code><return_msg><![CDATA[%s]]></return_msg></xml>",
            returnCode, returnMsg
        );
    }

    /**
     * 转换为支付宝回调响应格式
     */
    public String toAliResponse() {
        return "SUCCESS".equals(returnCode) ? "success" : "fail";
    }
}
