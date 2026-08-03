package plus.ruoyi.common.serialmap.core.impl;

import lombok.AllArgsConstructor;
import plus.ruoyi.common.core.service.UserService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

/**
 * 用户ID转昵称转换器
 *
 * <p>支持单个ID或多个ID（逗号分隔）转换为用户昵称
 *
 * @author 抓蛙师
 */
@SerialMapType(type = SerialMapConstant.USER_ID_TO_NICKNAME)
public class NickNameImpl implements SerialMapInterface<String> {

    private final UserService userService;

    /**
     * 构造方法
     */
    public NickNameImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof Long id) {
            return userService.getNickNamesByIds(id.toString());
        } else if (key instanceof String ids) {
            return userService.getNickNamesByIds(ids);
        }
        return null;
    }
}
