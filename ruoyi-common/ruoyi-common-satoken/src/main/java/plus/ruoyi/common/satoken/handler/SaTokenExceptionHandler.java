package plus.ruoyi.common.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.service.MenuService;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token异常处理器
 *
 * <p>统一处理Sa-Token框架抛出的认证授权异常，提供友好的错误响应：
 * <ul>
 *   <li>未登录异常：返回401状态码，提示用户重新登录</li>
 *   <li>权限不足异常：返回403状态码，显示缺少的权限及说明</li>
 *   <li>角色权限异常：返回403状态码，提示联系管理员授权</li>
 * </ul>
 *
 * <p>所有异常都会以 warn 级别记录详细日志（认证/授权失败属预期内客户端行为，非服务端 error），
 * 包含请求URI和异常信息，便于问题排查
 *
 * @author Lion Li
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class SaTokenExceptionHandler {

    @Autowired(required = false)
    private MenuService menuService;

    /**
     * 处理权限码校验失败异常
     *
     * <p>当用户缺少特定权限码时触发，返回403禁止访问，并显示缺少的权限详情
     *
     * @param e 权限异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String permission = e.getPermission();

        // 403 权限不足属于客户端访问控制结果（预期内），用 warn 而非 error，避免污染 error 日志
        log.warn("请求地址'{}',权限码校验失败,缺少权限'{}'", requestUri, permission);

        // 查询权限对应的菜单名称
        String menuName = null;
        if (menuService != null && StringUtils.isNotBlank(permission)) {
            try {
                menuName = menuService.getMenuNameByPerms(permission);
            } catch (Exception ex) {
                log.warn("查询权限[{}]对应的菜单名称失败: {}", permission, ex.getMessage());
            }
        }

        // 构建错误消息：传入菜单名称和权限码两个参数
        String message = MessageUtils.message(I18nKeys.Permission.NO_ACCESS, menuName, permission);
        return R.fail(HttpStatus.HTTP_FORBIDDEN, message);
    }

    /**
     * 处理角色权限校验失败异常
     *
     * <p>当用户不具备所需角色时触发，返回403禁止访问
     *
     * @param e 角色异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 403 角色不足属于客户端访问控制结果（预期内），用 warn 而非 error
        log.warn("请求地址'{}',角色权限校验失败'{}'", requestUri, e.getMessage());
        return R.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 处理用户未登录异常
     *
     * <p>当Token无效、过期或未提供时触发，返回401需要认证
     *
     * @param e 登录异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 401 未登录/token 过期是高频且预期内的客户端行为，用 warn 而非 error，避免淹没真正的服务端故障
        log.warn("请求地址'{}',认证失败'{}',无法访问系统资源", requestUri, e.getMessage());
        return R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
    }

}
