package plus.ruoyi.common.core.service;

import plus.ruoyi.common.core.domain.dto.OssDTO;

import java.util.List;

/**
 * 通用 OSS服务
 *
 * @author Lion Li
 */
public interface OssService {

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    String getUrlsByIds(String ossIds);

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    List<OssDTO> listOssByIds(String ossIds);

    /**
     * 根据目录id查询目录名称
     *
     * @param directoryId 目录id
     * @return 目录名称
     */
    String getDirectoryNameById(Long directoryId);

    /**
     * 根据URL获取OSS信息
     *
     * @param url 文件URL
     * @return OSS信息，如果找不到则返回null
     */
    OssDTO getOssByUrl(String url);
}
