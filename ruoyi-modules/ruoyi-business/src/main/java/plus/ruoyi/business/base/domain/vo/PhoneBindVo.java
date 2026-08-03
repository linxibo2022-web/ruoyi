package plus.ruoyi.business.base.domain.vo;

/**
 * @author 抓蛙师
 * @date 2025/8/21
 */

import cn.hutool.core.util.DesensitizedUtil;
import lombok.Data;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 手机号绑定结果视图对象
 */
@Data
public class PhoneBindVo {

    /**
     * 手机号（不含国家码）
     */
    private String phone;

    /**
     * 绑定时间戳
     */
    private Long bindTime;

    /**
     * 脱敏手机号（用于前端显示）
     */
    public String getMaskedPhone() {
        return DesensitizedUtil.mobilePhone(phone);
    }

    /**
     * 是否已绑定手机号
     */
    public boolean isBind() {
        return StringUtils.isNotBlank(phone);
    }

    /**
     * 静态构造方法
     */
    public static PhoneBindVo of(String phone) {
        PhoneBindVo phoneBindVo = new PhoneBindVo();
        phoneBindVo.setPhone(phone);
        return phoneBindVo;
    }

    /**
     * 静态构造方法
     */
    public static PhoneBindVo of(String phone, Long bindTime) {
        PhoneBindVo phoneBindVo = new PhoneBindVo();
        phoneBindVo.setPhone(phone);
        phoneBindVo.setBindTime(bindTime);
        return phoneBindVo;
    }
}
