package plus.ruoyi.system.monitor.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import plus.ruoyi.system.monitor.domain.bo.DevLogBo;
import plus.ruoyi.system.monitor.domain.dto.DevLogMessageDto;

import java.util.List;

/**
 * 开发环境日志监控
 * 仅在开发环境生效
 *
 * @author 抓蛙师
 */
@Profile({"dev", "test"})  // 仅在开发和测试环境启用
@Slf4j
@Validated
@RestController
@RequestMapping("/system/devLog")
public class DevLogController {

    /**
     * 收集前端日志
     * <p>
     * 接收前端发送的批量日志，通过WebSocket推送给监控页面（仅发送给超管）
     * 无需登录即可访问，方便收集未登录状态的日志
     *
     * @param bo 日志数据
     * @return 成功响应
     */
    @SaIgnore
    @PostMapping("/collect")
    public R<Void> collect(@Validated @RequestBody DevLogBo bo) {
        log.debug("收到前端日志 {} 条", bo.getLogs().size());

        // 构造日志消息DTO
        DevLogMessageDto messageDto = DevLogMessageDto.of(bo.getLogs());

        // 只发送给超级管理员
        WebSocketMessageDto wsMessageDto = WebSocketMessageDto.of(
            List.of(TenantConstants.SUPER_ADMIN_ID),
            JsonUtils.toJsonString(messageDto)
        );
        WebSocketUtils.publishMessage(wsMessageDto);

        return R.ok();
    }
}
