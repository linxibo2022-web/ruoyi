package plus.ruoyi.system.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.constant.CacheConstants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.dto.UserOnlineDTO;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.system.core.domain.vo.SysUserOnlineVo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线用户监控
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController {

    /**
     * 获取在线用户监控列表
     *
     * @param ipaddr   IP地址
     * @param userName 用户名
     */
    @SaCheckPermission("monitor:online:query")
    @GetMapping("/pageOnlineUsers")
    public R<PageResult<SysUserOnlineVo>> pageOnlineUsers(String ipaddr, String userName) {
        // 获取所有未过期的 token
        Collection<String> keys = RedisUtils.keys(CacheConstants.ONLINE_TOKEN_KEY + "*");
        List<UserOnlineDTO> userOnlineDTOList = new ArrayList<>();
        for (String key : keys) {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                continue;
            }
            userOnlineDTOList.add(RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + token));
        }
        if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr()) &&
                    StringUtils.equals(userName, userOnline.getUserName())
            );
        } else if (StringUtils.isNotEmpty(ipaddr)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr())
            );
        } else if (StringUtils.isNotEmpty(userName)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(userName, userOnline.getUserName())
            );
        }
        Collections.reverse(userOnlineDTOList);
        userOnlineDTOList.removeAll(Collections.singleton(null));
        List<SysUserOnlineVo> userOnlineList = BeanUtil.copyToList(userOnlineDTOList, SysUserOnlineVo.class);
        return R.ok(PageResult.of(userOnlineList));
    }

    /**
     * 强退用户
     *
     * @param tokenId token值
     */
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", operType = DictOperType.FORCE)
    @DeleteMapping("/forceLogout/{tokenId}")
    public R<Void> forceLogout(@NotBlank(message = "tokenId不为空") @PathVariable String tokenId) {
        try {
            StpUtil.kickoutByTokenValue(tokenId);
        } catch (NotLoginException ignored) {
        }
        return R.ok();
    }

    /**
     * 获取当前用户登录在线设备
     */
    @GetMapping("/listCurrentUserOnlines")
    public R<List<SysUserOnlineVo>> listCurrentUserOnlines() {
        // 获取指定账号 id 的 token 集合
        List<String> tokenIds = StpUtil.getTokenValueListByLoginId(StpUtil.getLoginIdAsString());
        List<UserOnlineDTO> userOnlineDTOList = tokenIds.stream()
            .filter(token -> StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) >= -1)
            .map(token -> (UserOnlineDTO) RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + token))
            .collect(Collectors.toList());
        //复制和处理 SysUserOnlineVo 对象列表
        Collections.reverse(userOnlineDTOList);
        userOnlineDTOList.removeAll(Collections.singleton(null));
        List<SysUserOnlineVo> userOnlineList = BeanUtil.copyToList(userOnlineDTOList, SysUserOnlineVo.class);
        return R.ok(userOnlineList);
    }

    /**
     * 强退当前在线设备
     *
     * @param tokenId token值
     */
    @Log(title = "在线设备", operType = DictOperType.FORCE)
    @DeleteMapping("/removeCurrentDevice/{tokenId}")
    public R<Void> removeCurrentDevice(@NotBlank(message = "tokenId不为空") @PathVariable("tokenId") String tokenId) {
        try {
            // 获取指定账号 id 的 token 集合
            List<String> keys = StpUtil.getTokenValueListByLoginId(StpUtil.getLoginIdAsString());
            keys.stream()
                .filter(key -> key.equals(tokenId))
                .findFirst()
                .ifPresent(key -> StpUtil.kickoutByTokenValue(tokenId));
        } catch (NotLoginException ignored) {
        }
        return R.ok();
    }
}
