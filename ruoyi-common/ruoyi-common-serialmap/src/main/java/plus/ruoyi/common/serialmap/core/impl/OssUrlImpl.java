package plus.ruoyi.common.serialmap.core.impl;

import plus.ruoyi.common.core.service.OssService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import lombok.AllArgsConstructor;

/**
 * OSS文件ID转URL转换器
 *
 * <p>支持单个ID或多个ID（逗号分隔）转换为文件访问URL
 *
 * @author 抓蛙师
 */
@SerialMapType(type = SerialMapConstant.OSS_ID_TO_URL)
public class OssUrlImpl implements SerialMapInterface<String> {

    private final OssService ossService;

    /**
     * 构造方法
     */
    public OssUrlImpl(OssService ossService) {
        this.ossService = ossService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof String ids) {
            return ossService.getUrlsByIds(ids);
        } else if (key instanceof Long id) {
            return ossService.getUrlsByIds(id.toString());
        }
        return null;
    }
}
