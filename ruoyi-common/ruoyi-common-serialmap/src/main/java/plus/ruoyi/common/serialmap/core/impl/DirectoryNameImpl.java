package plus.ruoyi.common.serialmap.core.impl;

import lombok.AllArgsConstructor;
import plus.ruoyi.common.core.service.OssService;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

/**
 * 目录ID转名称转换器
 *
 * <p>将OSS目录ID转换为目录名称
 *
 * @author 抓蛙师
 */
@SerialMapType(type = SerialMapConstant.DIRECTORY_ID_DIRECTORY_NAME)
public class DirectoryNameImpl implements SerialMapInterface<String> {

    private final OssService ossService;

    /**
     * 构造方法
     */
    public DirectoryNameImpl(OssService ossService) {
        this.ossService = ossService;
    }

    @Override
    public String convert(Object key, String param) {
        if(key instanceof Long id) {
            return ossService.getDirectoryNameById(id);
        }
        return null;
    }
}
