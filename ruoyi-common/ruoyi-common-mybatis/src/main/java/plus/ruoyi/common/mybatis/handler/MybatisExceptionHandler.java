package plus.ruoyi.common.mybatis.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.http.HttpStatus;
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import plus.ruoyi.common.core.domain.R;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mybatis异常处理器
 *
 * @author Lion Li
 */
@Slf4j
@RestControllerAdvice
@Order(1)  // 优先级高于GlobalExceptionHandler (Integer.MAX_VALUE)
public class MybatisExceptionHandler {

    /**
     * 主键或UNIQUE索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String errorMsg = e.getMessage();
        log.error("请求地址'{}',数据库中已存在记录'{}'", requestURI, errorMsg);

        // 解析重复的字段名并生成友好的错误提示
        String friendlyMessage = parseDuplicateKeyMessage(errorMsg);
        return R.fail(HttpStatus.HTTP_CONFLICT, friendlyMessage);
    }

    /**
     * 解析唯一约束冲突异常，提取字段名并生成友好提示
     *
     * @param errorMsg 异常消息
     * @return 友好的错误提示
     */
    private String parseDuplicateKeyMessage(String errorMsg) {
        if (errorMsg == null) {
            return "数据库中已存在该记录，请检查是否重复";
        }

        // MySQL 唯一约束异常消息示例：
        // Duplicate entry 'admin' for key 'sys_user.username'
        // Duplicate entry '13800138000' for key 'idx_phonenumber'
        // Duplicate entry '1989610002624942082' for key 'sys_user.PRIMARY'

        // 正则匹配：提取重复的值和字段名
        // 匹配模式: Duplicate entry 'value' for key 'table.field' 或 for key 'field'
        Pattern pattern = Pattern.compile(
            "Duplicate entry '([^']+)' for key ['\"`](?:\\w+\\.)?([\\w_]+)['\"`]",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(errorMsg);

        if (matcher.find()) {
            String duplicateValue = matcher.group(1);
            String fieldName = matcher.group(2).toLowerCase();

            // 特殊处理：主键冲突
            if ("primary".equals(fieldName)) {
                return String.format("主键冲突，ID[%s]已存在", duplicateValue);
            }

            // 去除可能的索引前缀（如 idx_、uk_、uni_ 等）
            fieldName = removeIndexPrefix(fieldName);

            return String.format("字段[%s]已存在，请更换后重试", fieldName);
        }

        // 降级处理：返回通用提示
        return "数据库中已存在该记录，请检查是否重复";
    }

    /**
     * 去除索引名的前缀
     *
     * @param indexName 索引名称
     * @return 去除前缀后的字段名
     */
    private String removeIndexPrefix(String indexName) {
        // 常见索引前缀
        String[] prefixes = {"idx_", "uk_", "uni_", "uniq_", "index_", "unique_"};

        for (String prefix : prefixes) {
            if (indexName.startsWith(prefix)) {
                return indexName.substring(prefix.length());
            }
        }

        return indexName;
    }

    /**
     * Mybatis系统异常 通用处理
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public R<Void> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Throwable root = getRootCause(e);

        // 检查是否是认证失败异常
        if (root instanceof NotLoginException) {
            log.error("请求地址'{}',认证失败'{}',无法访问系统资源", requestURI, root.getMessage());
            return R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
        }

        // 检查是否是数据源异常
        if (root instanceof CannotFindDataSourceException) {
            log.error("请求地址'{}', 未找到数据源", requestURI);
            return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, "未找到数据源，请联系管理员确认");
        }

        // 检查异常链中是否包含 DuplicateKeyException
        Throwable duplicateKeyCause = findCause(e, DuplicateKeyException.class);
        if (duplicateKeyCause != null) {
            log.error("请求地址'{}',数据库中已存在记录'{}'", requestURI, duplicateKeyCause.getMessage());
            String friendlyMessage = parseDuplicateKeyMessage(duplicateKeyCause.getMessage());
            return R.fail(HttpStatus.HTTP_CONFLICT, friendlyMessage);
        }

        log.error("请求地址'{}', Mybatis系统异常", requestURI, e);
        return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, e.getMessage());
    }

    /**
     * 获取异常的根因（递归查找）
     *
     * @param e 当前异常
     * @return 根因异常（最底层的 cause）
     * <p>
     * 逻辑说明：
     * 1. 如果 e 没有 cause，说明 e 本身就是根因，直接返回
     * 2. 如果 e 的 cause 和自身相同（防止循环引用），也返回 e
     * 3. 否则递归调用，继续向下寻找最底层的 cause
     */
    public static Throwable getRootCause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null || cause == e) {
            return e;
        }
        return getRootCause(cause);
    }

    /**
     * 在异常链中查找指定类型的异常
     *
     * @param e     当前异常
     * @param clazz 目标异常类
     * @return 找到的指定类型异常，如果没有找到返回 null
     */
    public static Throwable findCause(Throwable e, Class<? extends Throwable> clazz) {
        Throwable t = e;
        while (t != null && t != t.getCause()) {
            if (clazz.isInstance(t)) {
                return t;
            }
            t = t.getCause();
        }
        return null;
    }

}
