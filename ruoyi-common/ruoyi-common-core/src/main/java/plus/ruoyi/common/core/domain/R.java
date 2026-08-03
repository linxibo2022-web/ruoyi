package plus.ruoyi.common.core.domain;

import plus.ruoyi.common.core.constant.HttpStatus;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MessageUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.regex.RegexValidator;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体
 * <p>
 * 统一的API响应结果封装类，提供了成功、失败、警告等多种响应状态的便捷方法。
 * 遵循RESTful API设计规范，使用标准HTTP状态码表示操作结果。
 * 支持泛型，可以封装任意类型的响应数据。
 * 自动支持国际化消息处理，当消息符合国际化键格式时会自动进行翻译。
 *
 * @param <T> 响应数据的类型
 * @author Lion Li
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码 (200 OK)
     */
    public static final int SUCCESS = HttpStatus.SUCCESS;

    /**
     * 失败状态码 (500 Internal Server Error)
     */
    public static final int FAIL = HttpStatus.ERROR;

    /**
     * 消息状态码
     * 参考 {@link HttpStatus} 中定义的常量
     */
    private int code;

    /**
     * 消息内容
     * 用于描述操作结果的详细信息，支持国际化
     */
    private String msg;

    /**
     * 数据对象
     * 操作返回的实际数据，可以是任意类型
     */
    private T data;

    // ==================== 核心工具方法 ====================

    /**
     * 处理国际化消息
     * 使用预编译正则表达式判断是否为国际化键格式
     *
     * @param message 原始消息或国际化键
     * @param args    消息参数
     * @return 处理后的消息
     */
    private static String processMessage(String message, Object... args) {
        if (StringUtils.isBlank(message)) {
            return null;
        }
        // 使用正则表达式校验工具进行判断是否为国际化键
        if (RegexValidator.isValidI18nKey(message)) {
            return MessageUtils.message(message, args);
        }
        return message;
    }

    /**
     * 创建响应结果
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 响应结果
     */
    private static <T> R<T> restResult(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    // ==================== 成功响应方法 ====================

    /**
     * 返回成功消息
     *
     * @param <T> 响应数据类型
     * @return 成功消息，状态码为 200，默认消息为"操作成功"
     */
    public static <T> R<T> ok() {
        return restResult(SUCCESS, processMessage(I18nKeys.Oper.SUCCESS), null);
    }

    /**
     * 返回成功数据
     *
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 成功消息，状态码为 200，包含数据对象
     */
    public static <T> R<T> ok(T data) {
        return restResult(SUCCESS, processMessage(I18nKeys.Oper.SUCCESS), data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容（支持国际化键）
     * @param <T> 响应数据类型
     * @return 成功消息，状态码为 200，自定义消息内容
     */
    public static <T> R<T> ok(String msg) {
        return restResult(SUCCESS, processMessage(msg), null);
    }

    /**
     * 返回成功消息（带参数的国际化）
     *
     * @param msg  返回内容（支持国际化键）
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 成功消息，状态码为 200，自定义消息内容
     */
    public static <T> R<T> ok(String msg, Object... args) {
        return restResult(SUCCESS, processMessage(msg, args), null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 成功消息，状态码为 200，自定义消息内容和数据对象
     */
    public static <T> R<T> ok(String msg, T data) {
        return restResult(SUCCESS, processMessage(msg), data);
    }

    /**
     * 返回成功消息（带参数的国际化和数据）
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 成功消息，状态码为 200，自定义消息内容和数据对象
     */
    public static <T> R<T> ok(String msg, T data, Object... args) {
        return restResult(SUCCESS, processMessage(msg, args), data);
    }

    // ==================== 失败响应方法 ====================

    /**
     * 返回失败消息
     *
     * @param <T> 响应数据类型
     * @return 失败消息，状态码为 500，默认消息为"操作失败"
     */
    public static <T> R<T> fail() {
        return restResult(FAIL, processMessage(I18nKeys.Oper.FAIL), null);
    }

    /**
     * 返回失败消息
     *
     * @param msg 返回内容（支持国际化键）
     * @param <T> 响应数据类型
     * @return 失败消息，状态码为 500，自定义消息内容
     */
    public static <T> R<T> fail(String msg) {
        return restResult(FAIL, processMessage(msg), null);
    }

    /**
     * 返回失败消息（带参数的国际化）
     *
     * @param msg  返回内容（支持国际化键）
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 失败消息，状态码为 500，自定义消息内容
     */
    public static <T> R<T> fail(String msg, Object... args) {
        return restResult(FAIL, processMessage(msg, args), null);
    }

    /**
     * 返回失败数据
     *
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 失败消息，状态码为 500，包含数据对象
     */
    public static <T> R<T> fail(T data) {
        return restResult(FAIL, processMessage(I18nKeys.Oper.FAIL), data);
    }

    /**
     * 返回失败消息
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 失败消息，状态码为 500，自定义消息内容和数据对象
     */
    public static <T> R<T> fail(String msg, T data) {
        return restResult(FAIL, processMessage(msg), data);
    }

    /**
     * 返回失败消息（带参数的国际化和数据）
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 失败消息，状态码为 500，自定义消息内容和数据对象
     */
    public static <T> R<T> fail(String msg, T data, Object... args) {
        return restResult(FAIL, processMessage(msg, args), data);
    }

    /**
     * 返回失败消息
     *
     * @param code 状态码
     * @param msg  返回内容（支持国际化键）
     * @param <T>  响应数据类型
     * @return 失败消息，自定义状态码和消息内容
     */
    public static <T> R<T> fail(int code, String msg) {
        return restResult(code, processMessage(msg), null);
    }

    /**
     * 返回失败消息（带参数的国际化）
     *
     * @param code 状态码
     * @param msg  返回内容（支持国际化键）
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 失败消息，自定义状态码和消息内容
     */
    public static <T> R<T> fail(int code, String msg, Object... args) {
        return restResult(code, processMessage(msg, args), null);
    }

    // ==================== 警告响应方法 ====================

    /**
     * 返回警告消息
     *
     * @param msg 返回内容（支持国际化键）
     * @param <T> 响应数据类型
     * @return 警告消息，状态码为 601，自定义消息内容
     */
    public static <T> R<T> warn(String msg) {
        return restResult(HttpStatus.WARN, processMessage(msg), null);
    }

    /**
     * 返回警告消息（带参数的国际化）
     *
     * @param msg  返回内容（支持国际化键）
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 警告消息，状态码为 601，自定义消息内容
     */
    public static <T> R<T> warn(String msg, Object... args) {
        return restResult(HttpStatus.WARN, processMessage(msg, args), null);
    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 警告消息，状态码为 601，自定义消息内容和数据对象
     */
    public static <T> R<T> warn(String msg, T data) {
        return restResult(HttpStatus.WARN, processMessage(msg), data);
    }

    /**
     * 返回警告消息（带参数的国际化和数据）
     *
     * @param msg  返回内容（支持国际化键）
     * @param data 数据对象
     * @param args 消息参数
     * @param <T>  响应数据类型
     * @return 警告消息，状态码为 601，自定义消息内容和数据对象
     */
    public static <T> R<T> warn(String msg, T data, Object... args) {
        return restResult(HttpStatus.WARN, processMessage(msg, args), data);
    }

    // ==================== 状态判断和工具方法 ====================

    /**
     * 根据布尔值返回操作状态响应
     *
     * @param flag 操作是否成功
     * @param <T>  响应数据类型
     * @return 成功返回 ok()，失败返回 fail()
     */
    public static <T> R<T> status(boolean flag) {
        return flag ? ok() : fail();
    }

    /**
     * 根据影响行数返回操作状态响应
     * <p>
     * 当影响行数大于0时返回成功，否则返回失败。
     * 适用于数据库操作返回影响行数的场景。
     *
     * @param rows 影响行数
     * @param <T>  响应数据类型
     * @return 影响行数 > 0 返回 ok()，否则返回 fail()
     */
    public static <T> R<T> status(int rows) {
        return rows > 0 ? ok() : fail();
    }

    /**
     * 根据布尔值返回操作状态响应（自定义成功/失败消息）
     *
     * @param flag       操作是否成功
     * @param successMsg 成功消息（支持国际化键）
     * @param failMsg    失败消息（支持国际化键）
     * @param <T>        响应数据类型
     * @return 成功返回 ok(successMsg)，失败返回 fail(failMsg)
     */
    public static <T> R<T> status(boolean flag, String successMsg, String failMsg) {
        return flag ? ok(successMsg) : fail(failMsg);
    }

    /**
     * 根据布尔值返回响应，并附带数据
     *
     * @param flag 操作是否成功
     * @param data 数据对象
     * @param <T>  响应数据类型
     * @return 成功返回 ok(data)，失败返回 fail(data)
     */
    public static <T> R<T> status(boolean flag, T data) {
        return flag ? ok(data) : fail(data);
    }

    /**
     * 根据布尔值返回响应，并附带消息和数据
     *
     * @param flag       操作是否成功
     * @param successMsg 成功提示消息（支持国际化键）
     * @param failMsg    失败提示消息（支持国际化键）
     * @param data       数据对象
     * @param <T>        响应数据类型
     * @return 成功返回 ok(successMsg, data)，失败返回 fail(failMsg, data)
     */
    public static <T> R<T> status(boolean flag, String successMsg, String failMsg, T data) {
        return flag ? ok(successMsg, data) : fail(failMsg, data);
    }

    /**
     * 判断响应是否成功
     *
     * @param ret 响应对象
     * @param <T> 响应数据类型
     * @return true-成功；false-错误
     */
    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }

    /**
     * 判断响应是否为错误
     *
     * @param ret 响应对象
     * @param <T> 响应数据类型
     * @return true-错误；false-成功
     */
    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    /**
     * 获取响应数据
     * 如果响应成功则返回数据，否则返回null
     *
     * @param ret 响应对象
     * @param <T> 响应数据类型
     * @return 响应数据或null
     */
    public static <T> T getData(R<T> ret) {
        return isSuccess(ret) ? ret.getData() : null;
    }
}
