package plus.ruoyi.system.oss.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.validate.QueryGroup;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.oss.domain.bo.ConfirmDirectUploadBo;
import plus.ruoyi.system.oss.domain.bo.PresignedUrlBo;
import plus.ruoyi.system.oss.domain.bo.SysOssBo;
import plus.ruoyi.system.oss.domain.vo.PresignedUrlVo;
import plus.ruoyi.system.oss.domain.vo.SysOssUploadVo;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;
import plus.ruoyi.system.oss.service.ISysOssService;

/**
 * OSS对象存储 控制层
 *
 * @author Lion Li
 */
@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/oss")
public class SysOssController {

    // 业务服务
    private final ISysOssService ossService;

    /**
     * 查询OSS对象存储列表
     */
    @SaCheckPermission("system:oss:query")
    @GetMapping("/pageOss")
    public R<PageResult<SysOssVo>> pageOss(@Validated(QueryGroup.class) SysOssBo bo, PageQuery pageQuery) {
        PageResult<SysOssVo> pageVo = ossService.page(bo, pageQuery);
        // 处理OSS对象存储私有桶URL
        pageVo.getRecords().forEach(ossService::matchingUrl);
        return R.ok(pageVo);
    }

    /**
     * 查询OSS对象基于id串
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:query")
    @GetMapping("/listOssByIds/{ossIds}")
    public R<List<SysOssVo>> listOssByIds(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ossIds) {
        List<SysOssVo> list = ossService.listOssByIds(List.of(ossIds));
        return R.ok(list);
    }

    /**
     * 根据文件URL查询OSS对象ID
     *
     * @param url 文件URL
     * @return ossId 文件ID
     */
    @GetMapping("/getOssByUrl")
    public R<Long> getOssByUrl(@RequestParam("url") String url) {
        OssDTO ossFile = ossService.getOssByUrl(url);
        if (ossFile == null) {
            return R.fail("未找到对应的文件信息");
        }
        return R.ok(ossFile.getOssId());
    }

    /**
     * 上传OSS对象存储
     *
     * @param file          文件
     * @param moduleName    模块名称，可为空
     * @param directoryId   文件目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "OSS对象存储", operType = DictOperType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file,
                                    @RequestParam(value = "moduleName", required = false) String moduleName,
                                    @RequestParam(value = "directoryId", required = false) Long directoryId,
                                    @RequestParam(value = "directoryPath", required = false) String directoryPath) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        SysOssVo oss = ossService.upload(moduleName, directoryId, directoryPath, file);
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setOriginalUrl(oss.getOriginalUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId().toString());
        uploadVo.setUpdateTime(oss.getUpdateTime());
        return R.ok(uploadVo);
    }

    /**
     * 替换OSS对象存储
     *
     * @param ossId 要替换的文件ID
     * @param file  新文件
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "OSS对象存储", operType = DictOperType.UPDATE)
    @PostMapping(value = "/replace/{ossId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> replace(@NotNull(message = "文件ID不能为空") @PathVariable Long ossId,
                                     @RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        SysOssVo oss = ossService.replace(ossId, file);
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setOriginalUrl(oss.getOriginalUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId().toString());
        uploadVo.setUpdateTime(oss.getUpdateTime());
        return R.ok(uploadVo);
    }

    /**
     * 保存远程图片到OSS存储
     * 下载外部图片并保存到系统OSS存储，返回新的URL和图片信息
     *
     * @param imageUrl      远程图片URL
     * @param moduleName    模块名称，可为空
     * @param directoryId   文件目录id，可为空
     * @param directoryPath 目录路径，如：/文档/办公
     * @return 内部OSS图片信息
     */
    @PostMapping("/saveRemoteImageToOss")
    public R<SysOssUploadVo> convertImageUrl(String imageUrl,
                                             @RequestParam(value = "moduleName", required = false) String moduleName,
                                             @RequestParam(value = "directoryId", required = false) Long directoryId,
                                             @RequestParam(value = "directoryPath", required = false) String directoryPath) {
        if (ObjectUtil.isEmpty(imageUrl)) {
            return R.fail("图片URL不能为空");
        }

        try {
            // 检查是否已存在相同URL的记录
            OssDTO existingOss = ossService.getOssByUrl(imageUrl);
            if (ObjectUtil.isNotNull(existingOss)) {
                // 如果已经有相同URL的记录，直接返回
                SysOssVo existingVo = ossService.matchingUrl(MapstructUtils.convert(existingOss, SysOssVo.class));
                SysOssUploadVo uploadVo = new SysOssUploadVo();
                uploadVo.setUrl(existingVo.getUrl());
                uploadVo.setOriginalUrl(existingVo.getOriginalUrl());
                uploadVo.setFileName(existingOss.getOriginalName());
                uploadVo.setOssId(existingOss.getOssId().toString());
                uploadVo.setUpdateTime(existingOss.getUpdateTime());
                return R.ok(uploadVo);
            }

            // 下载外部图片
            SysOssVo oss = ossService.saveRemoteImageToOss(moduleName, directoryId, directoryPath, imageUrl);
            if (oss == null) {
                return R.fail("图片下载失败");
            }

            SysOssUploadVo uploadVo = new SysOssUploadVo();
            uploadVo.setUrl(oss.getUrl());
            uploadVo.setOriginalUrl(oss.getOriginalUrl());
            uploadVo.setFileName(oss.getOriginalName());
            uploadVo.setOssId(oss.getOssId().toString());
            uploadVo.setUpdateTime(oss.getUpdateTime());
            return R.ok(uploadVo);
        } catch (Exception e) {
            return R.fail("图片URL转换失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名上传URL
     *
     * @param presignedUrlBo 预签名请求参数
     */
    @SaCheckPermission("system:oss:upload")
    @PostMapping("/getPresignedUrl")
    public R<PresignedUrlVo> getPresignedUrl(@Validated @RequestBody PresignedUrlBo presignedUrlBo) {
        try {
            PresignedUrlVo result = ossService.generatePresignedUrl(
                presignedUrlBo.getFileName(),
                presignedUrlBo.getFileType(),
                presignedUrlBo.getModuleName(),
                presignedUrlBo.getDirectoryId(),
                presignedUrlBo.getDirectoryPath()
            );
            return R.ok(result);
        } catch (ServiceException e) {
            log.error("生成预签名URL失败", e);
            return R.fail("生成预签名URL失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            return R.fail("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 确认直传上传完成
     *
     * @param confirmDirectUploadBo 确认请求参数
     */
    @SaCheckPermission("system:oss:upload")
    @PostMapping("/confirmDirectUpload")
    public R<SysOssUploadVo> confirmDirectUpload(@Validated @RequestBody ConfirmDirectUploadBo confirmDirectUploadBo) {
        try {
            SysOssVo oss = ossService.confirmDirectUpload(
                confirmDirectUploadBo.getFileName(),
                confirmDirectUploadBo.getFileKey(),
                confirmDirectUploadBo.getFileUrl(),
                confirmDirectUploadBo.getModuleName(),
                confirmDirectUploadBo.getDirectoryId(),
                confirmDirectUploadBo.getDirectoryPath(),
                confirmDirectUploadBo.getFileSize()
            );

            SysOssUploadVo uploadVo = new SysOssUploadVo();
            uploadVo.setUrl(oss.getUrl());
            uploadVo.setOriginalUrl(oss.getOriginalUrl());
            uploadVo.setFileName(oss.getOriginalName());
            uploadVo.setOssId(oss.getOssId().toString());
            uploadVo.setUpdateTime(oss.getUpdateTime());
            return R.ok(uploadVo);
        } catch (Exception e) {
            log.error("确认直传上传失败", e);
            return R.fail("确认上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载OSS对象
     *
     * @param ossId OSS对象ID
     */
    @SaCheckPermission("system:oss:download")
    @GetMapping("/download/{ossId}")
    public void download(@PathVariable Long ossId, HttpServletResponse response) throws IOException {
        ossService.download(ossId, response);
    }

    /**
     * 删除OSS对象存储
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:delete")
    @Log(title = "OSS对象存储", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteOss/{ossIds}")
    public R<Void> deleteOss(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ossIds) {
        return R.status(ossService.batchDelete(List.of(ossIds)));
    }

    /**
     * 导出OSS对象存储列表
     */
    @SaCheckPermission("system:oss:export")
    @Log(title = "OSS对象存储", operType = DictOperType.EXPORT)
    @PostMapping("/exportOss")
    public void exportOss(SysOssBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysOssVo> pageResult = ossService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "OSS对象存储", SysOssVo.class, response);
    }
}
