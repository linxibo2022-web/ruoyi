package plus.ruoyi.common.doctemplate.exception;

/**
 * 文档模板处理异常
 *
 * @author 抓蛙师
 */
public class DocTemplateException extends RuntimeException {

    /**
     * 构造一个带有指定详细消息的文档模板异常
     *
     * @param message 异常的详细消息
     */
    public DocTemplateException(String message) {
        super(message);
    }

    /**
     * 构造一个带有指定详细消息和原因的文档模板异常
     *
     * @param message 异常的详细消息
     * @param cause   异常的原因
     */
    public DocTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个带有指定原因的文档模板异常
     *
     * @param cause 异常的原因
     */
    public DocTemplateException(Throwable cause) {
        super(cause);
    }
}
