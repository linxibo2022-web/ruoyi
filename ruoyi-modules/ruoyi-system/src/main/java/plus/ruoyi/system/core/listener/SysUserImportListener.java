package plus.ruoyi.system.core.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.http.HtmlUtil;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.excel.core.ExcelListener;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserImportVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.config.service.ISysConfigService;
import plus.ruoyi.system.core.service.ISysDeptService;
import plus.ruoyi.system.core.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 系统用户自定义导入
 *
 * @author Lion Li
 */
@Slf4j
public class SysUserImportListener extends AnalysisEventListener<SysUserImportVo> implements ExcelListener<SysUserImportVo> {

    private final ISysUserService userService;

    private final ISysDeptService deptService;

    private final String password;

    private final Long operUserId;

    private int successNum = 0;
    private int failureNum = 0;
    private final StringBuilder successMsg = new StringBuilder();
    private final StringBuilder failureMsg = new StringBuilder();

    public SysUserImportListener() {
        String initPassword = SpringUtils.getBean(ISysConfigService.class).getConfigByKey("system.user.initial-password");
        this.userService = SpringUtils.getBean(ISysUserService.class);
        this.deptService = SpringUtils.getBean(ISysDeptService.class);
        this.password = BCrypt.hashpw(initPassword);
        this.operUserId = LoginHelper.getUserId();
    }

    @Override
    public void invoke(SysUserImportVo userVo, AnalysisContext context) {
        SysUserVo sysUser = this.userService.getUserByUserName(userVo.getUserName());
        try {
            // 校验部门数据权限（防止导入其他部门的用户）
            deptService.checkDeptDataScope(userVo.getDeptId());

            // 验证是否存在这个用户
            if (ObjectUtil.isNull(sysUser)) {
                SysUserBo user = BeanUtil.toBean(userVo, SysUserBo.class);
                ValidatorUtils.validate(user);
                user.setPassword(password);
                user.setCreateBy(operUserId);
                userService.insertUser(user);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 导入成功");
            } else {
                Long userId = sysUser.getUserId();
                SysUserBo user = BeanUtil.toBean(userVo, SysUserBo.class);
                user.setUserId(userId);
                ValidatorUtils.validate(user);
                userService.checkUserAllowed(user.getUserId());
                userService.checkUserDataScope(user.getUserId());
                user.setUpdateBy(operUserId);
                userService.updateUser(user);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 更新成功");
            }
        } catch (Exception e) {
            failureNum++;
            String msg = "<br/>" + failureNum + "、账号 " + HtmlUtil.cleanHtmlTag(userVo.getUserName()) + " 导入失败：";
            String message = e.getMessage();
            if (e instanceof ConstraintViolationException cvException) {
                message = StreamUtils.join(cvException.getConstraintViolations(), ConstraintViolation::getMessage, ", ");
            }
            failureMsg.append(msg).append(message);
            log.error(msg, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    @Override
    public ExcelResult<SysUserImportVo> getExcelResult() {
        return new ExcelResult<>() {

            @Override
            public String getAnalysis() {
                if (failureNum > 0) {
                    failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
                    throw ServiceException.of(failureMsg.toString());
                } else {
                    successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
                }
                return successMsg.toString();
            }

            @Override
            public Map<Integer, String> getHead() {
                return null;
            }

            @Override
            public List<SysUserImportVo> getList() {
                return null;
            }

            @Override
            public List<String> getErrorList() {
                return null;
            }
        };
    }
}
