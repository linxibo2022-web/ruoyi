package plus.ruoyi.business.base.domain.vo;

import lombok.Data;

/**
 * 平台的用户信息(小程序/公众号/开放平台等)
 * @author 抓蛙师
 * @date 2025/6/22
 */
@Data
public class PlatformUserInfoVo {

    /**
     * 平台类型如mp-weixin, mp-qq等
     */
    private String platform;

    /**
     * appid
     */
    private String appid;

    /**
     * 用户统一标识
     */
    private String unionid;

    /**
     * 用户唯一标识
     */
    private String openid;


    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 性别
     */
    private String gender;

    /**
     * 凭证
     */
    private Object credential;

}
