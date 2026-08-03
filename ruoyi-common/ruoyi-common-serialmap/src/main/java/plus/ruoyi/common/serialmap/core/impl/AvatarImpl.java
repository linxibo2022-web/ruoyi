package plus.ruoyi.common.serialmap.core.impl;

import lombok.AllArgsConstructor;
import plus.ruoyi.common.core.service.UserService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

/**
 * 用户ID转头像转换器
 *
 * <p>支持单个ID或多个ID（逗号分隔）转换为用户头像
 *
 * @author 抓蛙师
 */
@SerialMapType(type = SerialMapConstant.USER_ID_TO_AVATAR)
public class AvatarImpl implements SerialMapInterface<String> {

    private final UserService userService;

    /**
     * 构造方法
     */
    public AvatarImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof Long id) {
            return userService.getAvatarsByIds(id.toString());
        } else if (key instanceof String ids) {
            return userService.getAvatarsByIds(ids);
        }
        return null;
    }
}
