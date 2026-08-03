package plus.ruoyi.common.serialmap.core.impl;

import plus.ruoyi.common.core.service.UserService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import lombok.AllArgsConstructor;

/**
 * 用户ID转用户名转换器
 *
 * <p>将用户ID转换为用户登录账号
 *
 * @author Lion Li
 */
@SerialMapType(type = SerialMapConstant.USER_ID_TO_NAME)
public class UserNameImpl implements SerialMapInterface<String> {

    private final UserService userService;

    /**
     * 构造方法
     */
    public UserNameImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof Long id) {
            return userService.getUserNameById(id);
        }
        return null;
    }
}
