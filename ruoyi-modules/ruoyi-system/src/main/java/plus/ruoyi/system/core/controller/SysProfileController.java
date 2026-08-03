package plus.ruoyi.system.core.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileTypeUtils;
import plus.ruoyi.common.encrypt.annotation.ApiEncrypt;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.bo.SysUserPasswordBo;
import plus.ruoyi.system.core.domain.bo.SysUserProfileBo;
import plus.ruoyi.system.core.domain.vo.AvatarVo;
import plus.ruoyi.system.core.domain.vo.ProfileVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;
import plus.ruoyi.system.oss.service.ISysOssService;

/**
 * 个人信息 业务处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysProfileController {

    /* 业务服务 */
    private final ISysUserService userService;
    private final ISysOssService ossService;

    /**
     * 个人信息
     */
    @GetMapping("/getUserProfile")
    public R<ProfileVo> getUserProfile() {
        SysUserVo user = userService.getUserWithRolesById(LoginHelper.getUserId());
        ProfileVo profileVo = new ProfileVo();
        profileVo.setUser(user);
        profileVo.setRoleGroup(userService.getUserRoleGroup(user.getUserId()));
        profileVo.setPostGroup(userService.getUserPostGroup(user.getUserId()));
        return R.ok(profileVo);
    }

    /**
     * 修改用户信息
     */
    @RepeatSubmit
    @Log(title = "个人信息", operType = DictOperType.UPDATE)
    @PutMapping("updateUserProfile")
    public R<Void> updateUserProfile(@Validated @RequestBody SysUserProfileBo profileBo) {
        SysUserBo sysUserBo = BeanUtil.toBean(profileBo, SysUserBo.class);
        sysUserBo.setUserId(LoginHelper.getUserId());
        String userName = LoginHelper.getUserName();
        if (StringUtils.isNotEmpty(sysUserBo.getPhone()) && !userService.isPhoneUnique(sysUserBo.getPhone(), sysUserBo.getUserId())) {
            return R.fail("修改用户'" + userName + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(sysUserBo.getEmail()) && !userService.isEmailUnique(sysUserBo.getEmail(), sysUserBo.getUserId())) {
            return R.fail("修改用户'" + userName + "'失败，邮箱账号已存在");
        }
        DataPermissionHelper.ignore(() -> userService.updateUserProfile(sysUserBo));
        return R.ok();
    }

    /**
     * 重置密码
     *
     * @param bo 新旧密码
     */
    @RepeatSubmit
    @ApiEncrypt
    @Log(title = "个人信息", operType = DictOperType.UPDATE)
    @PutMapping("/updateUserPwd")
    public R<Void> updateUserPwd(@Validated @RequestBody SysUserPasswordBo bo) {
        SysUserVo user = userService.getUserWithRolesById(LoginHelper.getUserId());
        String password = user.getPassword();
        if (!BCrypt.checkpw(bo.getOldPassword(), password)) {
            return R.fail("修改密码失败，旧密码错误");
        }
        if (BCrypt.checkpw(bo.getNewPassword(), password)) {
            return R.fail("新密码不能与旧密码相同");
        }
        DataPermissionHelper.ignore(() -> userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(bo.getNewPassword())));
        return R.ok();
    }

    /**
     * 头像上传
     *
     * @param avatarfile 用户头像
     */
    @RepeatSubmit
    @Log(title = "用户头像", operType = DictOperType.UPDATE)
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<AvatarVo> uploadAvatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        if (!avatarfile.isEmpty()) {
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!FileTypeUtils.isImage(extension)) {
                return R.fail("文件格式不正确，请上传" + FileTypeUtils.IMAGE_EXTENSION + "格式");
            }
            SysOssVo oss = ossService.upload("avatar", null, "/头像", avatarfile);
            String avatar = oss.getUrl();
            boolean updateSuccess = DataPermissionHelper.ignore(() -> userService.updateUserAvatar(LoginHelper.getUserId(), avatar));
            if (updateSuccess) {
                AvatarVo avatarVo = new AvatarVo();
                avatarVo.setImgUrl(avatar);
                return R.ok(avatarVo);
            }
        }
        return R.fail("上传图片异常，请联系管理员");
    }
}
