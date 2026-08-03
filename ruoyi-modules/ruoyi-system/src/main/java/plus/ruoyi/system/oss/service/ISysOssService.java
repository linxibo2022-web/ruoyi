package plus.ruoyi.system.oss.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.oss.domain.bo.SysOssBo;
import plus.ruoyi.system.oss.domain.vo.PresignedUrlVo;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 文件上传 服务层
 *
 * @author Lion Li
 */
public interface ISysOssService {

    /**
     * 根据ID查询
     *
     * @param ossId 主键ID
     * @return 视图对象
     */
    SysOssVo get(Long ossId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysOssVo> list(SysOssBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysOssVo> page(SysOssBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysOssBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysOssBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<SysOssBo> boList);

    /**
     * 根据文件名匹配 URL
     */
    SysOssVo matchingUrl(SysOssVo oss);

    /**
     * 根据一组 ossIds 获取对应的 SysOssVo 列表
     *
     * @param ossIds 一组文件在数据库中的唯一标识集合
     * @return 包含 SysOssVo 对象的列表
     */
    List<SysOssVo> listOssByIds(Collection<Long> ossIds);

    /**
     * 根据 ossId 从缓存或数据库中获取 SysOssVo 对象
     *
     * @param ossId 文件在数据库中的唯一标识
     * @return SysOssVo 对象，包含文件信息
     */
    SysOssVo getOssById(Long ossId);

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param file          要上传的 MultipartFile 对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     */
    SysOssVo upload(String moduleName, Long directoryId, String directoryPath, MultipartFile file);

    /**
     * 上传文件到对象存储服务，并保存文件信息到数据库
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param file          要上传的文件对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     */
    SysOssVo upload(String moduleName, Long directoryId, String directoryPath, File file);

    /**
     * 文件下载方法，支持一次性下载完整文件
     *
     * @param ossId    OSS对象ID
     * @param response HttpServletResponse对象，用于设置响应头和向客户端发送文件内容
     */
    void download(Long ossId, HttpServletResponse response) throws IOException;

    /**
     * 根据链接删除文件
     */
    boolean deleteByUrls(@NotBlank(message = "链接不能为空") String urls);

    /**
     * 替换文件
     *
     * @param ossId 要替换的文件ID
     * @param file  新上传的文件
     * @return 替换后的文件信息
     */
    SysOssVo replace(Long ossId, MultipartFile file);

    /**
     * 根据链接获取oss文件信息
     *
     * @param url 链接
     * @return 文件信息
     */
    OssDTO getOssByUrl(String url);

    /**
     * 通过远程URL保存图片到OSS
     *
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param url           远程图片URL
     * @return 保存后的OSS文件信息
     */
    SysOssVo saveRemoteImageToOss(String moduleName, Long directoryId, String directoryPath, String url);

    /**
     * 生成预签名上传URL
     *
     * @param fileName      文件名
     * @param fileType      文件类型
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @return 预签名URL信息
     */
    PresignedUrlVo generatePresignedUrl(String fileName, String fileType, String moduleName, Long directoryId, String directoryPath);

    /**
     * 确认直传上传并保存记录
     *
     * @param fileName      文件名
     * @param fileKey       文件键
     * @param fileUrl       文件URL
     * @param moduleName    模块名称，可为空
     * @param directoryId   目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @param fileSize      文件大小
     * @return OSS文件信息
     */
    SysOssVo confirmDirectUpload(String fileName, String fileKey, String fileUrl, String moduleName, Long directoryId, String directoryPath, Long fileSize);
}
