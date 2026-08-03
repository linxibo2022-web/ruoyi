package plus.ruoyi.system.core.domain.vo;

import lombok.Data;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;

/**
 * 用户头像信息
 *
 * @author Michelle.Chung
 */
@Data
public class AvatarVo {

    /**
     * 头像地址
     */
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String imgUrl;

}
